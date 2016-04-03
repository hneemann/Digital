package de.neemann.digital.gui.components.data;

import javax.swing.*;
import java.awt.*;

/**
 * The component to show the trace window.
 * It shows the data in the given dataSet.
 *
 * @author hneemann
 */
public class DataSetComponent extends JComponent {
    private static final int BORDER = 10;
    private static final int SIZE = 20;
    private static final int SEP2 = 3;
    private static final int SEP = SEP2 * 2;
    private static final Stroke NORMAL = new BasicStroke(0);
    private static final Stroke THICK = new BasicStroke(2);
    private static final int MIN_COUNT = 20;
    private final DataSet dataSet;
    private int textWidth;

    /**
     * Creates a new dataSet
     *
     * @param dataSet the dataSet to paint
     */
    public DataSetComponent(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        textWidth = 0;
        for (int i = 0; i < dataSet.signalSize(); i++) {
            String text = dataSet.getSignal(i).getName();
            int w = g.getFontMetrics().stringWidth(text);
            if (w > textWidth) textWidth = w;
        }
        int x = textWidth + BORDER + SEP;

        int yOffs = SIZE / 2 + g.getFontMetrics().getHeight() / 2;
        g.setColor(Color.BLACK);
        int y = BORDER;
        for (int i = 0; i < dataSet.signalSize(); i++) {
            String text = dataSet.getSignal(i).getName();
            g2.setColor(Color.BLACK);
            g.drawString(text, BORDER, y + yOffs);
            g2.setColor(Color.LIGHT_GRAY);
            g.drawLine(x, y - SEP2, x + SIZE * dataSet.size(), y - SEP2);
            y += SIZE + SEP;
        }
        g.drawLine(x, y - SEP2, x + SIZE * dataSet.size(), y - SEP2);


        int[] lastRy = new int[dataSet.signalSize()];
        boolean first = true;
        for (DataSample s : dataSet) {
            g2.setStroke(NORMAL);
            g2.setColor(Color.LIGHT_GRAY);
            g.drawLine(x, BORDER - SEP2, x, (SIZE + SEP) * dataSet.signalSize() + BORDER - SEP2);
            g2.setStroke(THICK);
            g2.setColor(Color.BLACK);
            y = BORDER;
            for (int i = 0; i < dataSet.signalSize(); i++) {

                long width = dataSet.getWidth(i);
                if (width == 0) width = 1;
                //int ry = (int) (SIZE-(SIZE*(s.getValue(i)-dataSet.getMin().getValue(i)))/ width);
                int ry = (int) (SIZE - (SIZE * s.getValue(i)) / width);
                g.drawLine(x, y + ry, x + SIZE, y + ry);
                if (!first && ry != lastRy[i])
                    g.drawLine(x, y + lastRy[i], x, y + ry);

                lastRy[i] = ry;
                y += SIZE + SEP;
            }
            first = false;
            x += SIZE;
        }
        g2.setStroke(NORMAL);
        g2.setColor(Color.LIGHT_GRAY);
        g.drawLine(x, BORDER - SEP2, x, (SIZE + SEP) * dataSet.signalSize() + BORDER - SEP2);
    }

    @Override
    public Dimension getPreferredSize() {
        int count = dataSet.size();
        if (count < MIN_COUNT) count = MIN_COUNT;
        return new Dimension(SIZE * count + BORDER * 2 + textWidth, (SIZE + SEP) * dataSet.signalSize() + BORDER * 2);
    }
}
