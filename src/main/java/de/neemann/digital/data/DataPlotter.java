/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.data;

import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.Drawable;

/**
 * The dataSet stores the collected DataSamples.
 * Every DataSample contains the values of al signals at a given time.
 */
public class DataPlotter implements Drawable {
    private final ValueTable dataOriginal;
    private final int maxTextLength;
    private double size = SIZE;
    private SyncAccess modelSync = SyncAccess.NOSYNC;

    /**
     * Creates a new instance
     *
     * @param data the signals used to collect DataSamples
     */
    public DataPlotter(ValueTable data) {
        this.dataOriginal = data;
        int tl = 0;
        for (int i = 0; i < data.getColumns(); i++) {
            String text = data.getColumnName(i);
            int w = text.length();
            if (w > tl) tl = w;
        }
        maxTextLength = tl;
    }

    private static final int BORDER = 10;
    private static final int SIZE = 25;
    private static final int SEP2 = 5;
    private static final int SEP = SEP2 * 2;

    /**
     * Fits the data in the visible area
     *
     * @param width width of the frame
     */
    public void fitInside(int width) {
        modelSync.access(() -> size = ((double) (width - getTextBorder())) / dataOriginal.getRows());
    }

    /**
     * Apply a scaling factor
     *
     * @param f the factor
     * @return the scaling factor really applied
     */
    public double scale(double f) {
        double oldSize = size;
        size *= f;
        if (size < Style.NORMAL.getThickness()) size = Style.NORMAL.getThickness();
        if (size > SIZE) size = SIZE;
        return size / oldSize;
    }

    @Override
    public void drawTo(Graphic g, Style highLight) {
        ValueTable data;
        if (modelSync == SyncAccess.NOSYNC) {
            data = dataOriginal;
        } else {
            data = modelSync.access(new Runnable() {
                private ValueTable data;

                @Override
                public void run() {
                    data = new ValueTable(dataOriginal);
                }
            }).data;
        }

        int x = getTextBorder();

        int yOffs = SIZE / 2;
        int y = BORDER;
        int signals = data.getColumns();
        for (int i = 0; i < signals; i++) {
            String text = data.getColumnName(i);
            g.drawText(new Vector(x - 2, y + yOffs), new Vector(x + 1, y + yOffs), text, Orientation.RIGHTCENTER, Style.NORMAL);
            g.drawLine(new Vector(x, y - SEP2), new Vector(x + (int) (size * data.getRows()), y - SEP2), Style.DASH);
            y += SIZE + SEP;
        }
        g.drawLine(new Vector(x, y - SEP2), new Vector(x + (int) (size * data.getRows()), y - SEP2), Style.DASH);


        LastState[] last = new LastState[signals];
        for (int i = 0; i < signals; i++) last[i] = new LastState();

        boolean first = true;
        double pos = 0;
        for (Value[] s : data) {
            int xx = (int) (pos + x);
            g.drawLine(new Vector(xx, BORDER - SEP2), new Vector(xx, (SIZE + SEP) * signals + BORDER - SEP2), Style.DASH);
            y = BORDER;
            for (int i = 0; i < signals; i++) {
                Style style;
                switch (s[i].getState()) {
                    case FAIL:
                        style = Style.FAILED;
                        break;
                    case PASS:
                        style = Style.PASS;
                        break;
                    default:
                        style = Style.NORMAL;
                }

                long width = data.getMax(i);
                if (width == 0) width = 1;
                int ry;
                final long value = s[i].getValue();
                ry = (int) (SIZE - (SIZE * value) / width);

                if (value != last[i].value)
                    last[i].hasChanged = true;

                if (width > 4 && last[i].textWidth == 0 && last[i].hasChanged) {
                    final String text = IntFormat.toShortHex(value);
                    last[i].textWidth = text.length() * SIZE / 2;
                    if (value < width / 2)
                        g.drawText(new Vector(xx + 1, y - SEP2 + 1), new Vector(xx + 2, y - SEP2 + 1), text, Orientation.LEFTTOP, Style.SHAPE_PIN);
                    else
                        g.drawText(new Vector(xx + 1, y + SIZE + SEP2 - 1), new Vector(xx + 2, y + SIZE + SEP2 - 1), text, Orientation.LEFTBOTTOM, Style.SHAPE_PIN);
                    last[i].hasChanged = false;
                }

                if (!s[i].getType().equals(Value.Type.HIGHZ))
                    g.drawLine(new Vector(xx, y + ry), new Vector((int) (xx + size), y + ry), style);

                if (!first && ry != last[i].y)
                    g.drawLine(new Vector(xx, y + last[i].y), new Vector(xx, y + ry), style);

                if (!first && value != last[i].value && Math.abs(ry - last[i].y) < SEP2)
                    g.drawLine(new Vector(xx, y + ry - SEP2), new Vector(xx, y + ry + SEP2), Style.NORMAL);

                last[i].y = ry;
                last[i].value = value;
                last[i].decTextWidth(size);

                y += SIZE + SEP;
            }
            first = false;
            pos += size;
        }
        g.drawLine(new Vector((int) (pos + x), BORDER - SEP2), new Vector((int) (pos + x), (SIZE + SEP) * signals + BORDER - SEP2), Style.DASH);
    }

    private int getTextBorder() {
        return maxTextLength * Style.NORMAL.getFontSize() / 2 + BORDER + SEP;
    }

    /**
     * @return the preferred height of the graphical representation
     */
    public int getGraphicHeight() {
        return dataOriginal.getColumns() * (SIZE + SEP) + 2 * BORDER;
    }

    /**
     * @return the current width of the graphical representation
     */
    public int getCurrentGraphicWidth() {
        return modelSync.access(new Runnable() {
            private int r;

            @Override
            public void run() {
                r = DataPlotter.this.getTextBorder() + (int) (dataOriginal.getRows() * size);
            }
        }).r;
    }

    /**
     * Sets lock to access the data
     *
     * @param modelSync the lock
     * @return this for chained calls
     */
    public DataPlotter setModelSync(SyncAccess modelSync) {
        this.modelSync = modelSync;
        return this;
    }

    private static final class LastState {
        private long value;
        private int y;
        private int textWidth;
        private boolean hasChanged = true;

        private void decTextWidth(double size) {
            if (textWidth > 0) {
                textWidth -= size;
                if (textWidth < 0)
                    textWidth = 0;
            }
        }
    }
}
