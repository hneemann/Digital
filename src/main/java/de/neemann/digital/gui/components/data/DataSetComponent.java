package de.neemann.digital.gui.components.data;

import de.neemann.digital.draw.graphics.GraphicSwing;

import javax.swing.*;
import java.awt.*;

/**
 * The component to show the trace window.
 * It shows the data in the given dataSet.
 *
 * @author hneemann
 */
public class DataSetComponent extends JComponent {
    private final DataSet dataSet;
    private JScrollPane scrollPane;

    /**
     * Creates a new dataSet
     *
     * @param dataSet the dataSet to paint
     */
    public DataSetComponent(DataSet dataSet) {
        this.dataSet = dataSet;
        addMouseWheelListener(e -> {
            double f = Math.pow(0.9, e.getWheelRotation());
            scale(f, e.getX());
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        dataSet.drawTo(new GraphicSwing(g2), null);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = dataSet.getCurrentGraphicWidth();
        if (w < 600) w = 600;
        return new Dimension(w, dataSet.getGraphicHeight());
    }

    /**
     * Apply a scaling factor
     *
     * @param f    the factor
     * @param xPos fixed position
     */
    public void scale(double f, int xPos) {
        revalidate();
        repaint();
        dataSet.scale(f);

        int x = (int) (xPos * f) - (xPos - (int) scrollPane.getViewport().getViewRect().getX());
        if (x < 0) x = 0;
        scrollPane.getViewport().setViewPosition(new Point(x, 0));
    }

    /**
     * Fits the data to the visible area
     *
     * @param width the clients width
     */
    public void fitData(int width) {
        dataSet.fitInside(width);
        revalidate();
        repaint();
    }

    /**
     * Sets the used scroll pane
     *
     * @param scrollPane the scroll pane witch contains this component
     */
    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }
}
