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
import de.neemann.digital.testing.parser.TestRow;

import javax.swing.*;

/**
 * The dataSet stores the collected DataSamples.
 * Every DataSample contains the values of all signals at a given time.
 */
public class DataPlotter implements Drawable {
    private final ValueTable dataOriginal;
    private final int textWidth;
    private double size = SIZE;
    private int offset = 0;
    private int width = 0;
    private boolean manualScaling = false;
    private SyncAccess modelSync = SyncAccess.NOSYNC;
    private JScrollBar scrollBar;
    private int autoScaleOffset;

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
        textWidth = tl * Style.NORMAL.getFontSize() / 2 + BORDER + SEP;
    }

    private static final int BORDER = 10;
    private static final int SIZE = 25;
    private static final int CENTER = SIZE / 2;
    private static final int SEP2 = 5;
    private static final int SEP = SEP2 * 2;

    /**
     * Fits the data in the visible area
     */
    public void fitInside() {
        modelSync.modify(() -> size = ((double) (width - textWidth)) / dataOriginal.getRows());
        offset = 0;
        manualScaling = false;
    }

    /**
     * Apply a scaling factor
     *
     * @param f    the factor
     * @param xPos actual mouse position
     */
    public void scale(double f, int xPos) {
        double p = (xPos - textWidth + offset) / size;

        size *= f;
        if (size < Style.NORMAL.getThickness()) size = Style.NORMAL.getThickness();
        if (size > SIZE * 6) size = SIZE * 6;

        offset = (int) (p * size - xPos + textWidth);

        manualScaling = true;
    }

    /**
     * Moves the plot
     *
     * @param dx the displacement
     */
    public void move(int dx) {
        offset -= dx;
        manualScaling = dx >= 0 || offset < autoScaleOffset;
    }

    @Override
    public void drawTo(Graphic g, Style highLight) {
        ValueTable data;
        final boolean staticData = modelSync == SyncAccess.NOSYNC;
        if (staticData) {
            data = dataOriginal;
        } else {
            data = modelSync.read(new Runnable() {
                private ValueTable data;

                @Override
                public void run() {
                    data = new ValueTable(dataOriginal);
                }
            }).data;
        }

        final int availDataWidth = width - textWidth;
        final int preferredDataWidth = (int) (size * data.getRows());

        autoScaleOffset = preferredDataWidth - availDataWidth + 2;
        if (!manualScaling && width > 0 && !staticData && autoScaleOffset > 0)
            offset = autoScaleOffset;

        if (scrollBar != null)
            scrollBar.setValues(offset, availDataWidth, 0, preferredDataWidth);

        int dataAreaWidth = availDataWidth;
        // if no width is given, plot all the data
        if (width == 0)
            dataAreaWidth = preferredDataWidth - offset;

        int yOffs = SIZE / 2;
        int y = BORDER;
        int signals = data.getColumns();
        int textPos = textWidth;
        if (offset < 0)
            textPos = textWidth - offset;
        for (int i = 0; i < signals; i++) {
            String text = data.getColumnName(i);
            g.drawText(new Vector(textPos - 2, y + yOffs), text, Orientation.RIGHTCENTER, Style.NORMAL);
            g.drawLine(new Vector(textPos, y - SEP2), new Vector(textWidth + dataAreaWidth, y - SEP2), Style.DASH);
            y += SIZE + SEP;
        }
        g.drawLine(new Vector(textPos, y - SEP2), new Vector(textWidth + dataAreaWidth, y - SEP2), Style.DASH);


        LastState[] last = new LastState[signals];
        for (int i = 0; i < signals; i++) last[i] = new LastState();

        boolean first = true;
        double pos = 0;
        for (TestRow s : data) {
            int x1 = (int) (pos + textWidth - offset);
            int x2 = (int) (pos + textWidth - offset + size);

            if (x2 > textWidth && x1 < textWidth + dataAreaWidth) {
                if (x1 < textWidth)
                    x1 = textWidth;
                if (x2 > textWidth + dataAreaWidth)
                    x2 = textWidth + dataAreaWidth;

                g.drawLine(new Vector(x1, BORDER - SEP2), new Vector(x1, (SIZE + SEP) * signals + BORDER - SEP2), Style.DASH);
                y = BORDER;
                for (int i = 0; i < signals; i++) {
                    Style style;
                    switch (s.getValue(i).getState()) {
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
                    long value = s.getValue(i).getValue();
                    int ry;
                    long sWidth = (width >>> 32);
                    if (sWidth == 0) {
                        ry = (int) (SIZE - (SIZE * value) / width);
                    } else {
                        ry = (int) (SIZE - (SIZE * (value >>> 32)) / sWidth);
                    }

                    if (value != last[i].value)
                        last[i].hasChanged = true;

                    if (width > 4 && last[i].textWidth == 0 && last[i].hasChanged) {
                        final String text = IntFormat.toShortHex(value);
                        last[i].textWidth = text.length() * SIZE / 2;
                        if (ry > CENTER)
                            g.drawText(new Vector(x1 + 1, y - SEP2 + 1), text, Orientation.LEFTTOP, Style.SHAPE_PIN);
                        else
                            g.drawText(new Vector(x1 + 1, y + SIZE + SEP2 - 1), text, Orientation.LEFTBOTTOM, Style.SHAPE_PIN);
                        last[i].hasChanged = false;
                    }

                    if (!s.getValue(i).getType().equals(Value.Type.HIGHZ))
                        g.drawLine(new Vector(x1, y + ry), new Vector(x2, y + ry), style);

                    if (!first && ry != last[i].y)
                        g.drawLine(new Vector(x1, y + last[i].y), new Vector(x1, y + ry), style);

                    if (!first && value != last[i].value && Math.abs(ry - last[i].y) < SEP2)
                        g.drawLine(new Vector(x1, y + ry - SEP2), new Vector(x1, y + ry + SEP2), Style.NORMAL);

                    last[i].y = ry;
                    last[i].value = value;
                    last[i].decTextWidth(x2 - x1);

                    y += SIZE + SEP;
                }
                first = false;
            }

            if (width > 0 && x1 > width)
                break;

            pos += size;

        }
        g.drawLine(new Vector(textWidth + dataAreaWidth, BORDER - SEP2), new Vector(textWidth + dataAreaWidth, (SIZE + SEP) * signals + BORDER - SEP2), Style.DASH);
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
        return modelSync.read(new Runnable() {
            private int r;

            @Override
            public void run() {
                r = DataPlotter.this.textWidth + (int) ((dataOriginal.getRows() + 1) * size);
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

    /**
     * Sets the width of the parents container
     *
     * @param width the component width
     */
    public void setWidth(int width) {
        this.width = width;
        if (scrollBar != null)
            scrollBar.setVisibleAmount(width - textWidth);
    }

    /**
     * Sets the scroll bar to use
     *
     * @param scrollBar the scroll bar
     */
    public void setScrollBar(JScrollBar scrollBar) {
        this.scrollBar = scrollBar;
    }

    /**
     * Sets the new offset.
     * Is called by the scrollbar.
     *
     * @param value the new offset
     * @return true if there was a change
     */
    public boolean setNewOffset(int value) {
        if (offset != value) {
            offset = value;
            manualScaling = scrollBar == null || scrollBar.getMaximum() - scrollBar.getVisibleAmount() != offset;
            return true;
        }
        return false;
    }

    private static final class LastState {
        private long value;
        private int y;
        private int textWidth;
        private boolean hasChanged = true;

        private void decTextWidth(int size) {
            if (textWidth > 0) {
                textWidth -= size;
                if (textWidth < 0)
                    textWidth = 0;
            }
        }
    }
}
