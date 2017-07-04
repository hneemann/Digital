package de.neemann.digital.gui.components.data;

import de.neemann.digital.data.DataPlotter;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.gui.sync.Sync;

import javax.swing.*;
import java.awt.*;

/**
 * The component to show the trace window.
 * It shows the data in the given dataSet.
 *
 * @author hneemann
 */
public class GraphComponent extends JComponent {
    private final DataPlotter plotter;
    /**
     * The data stored in the plotter needs to be seen as part of the model.
     * So a lock is necessary to access the data.
     */
    private JScrollPane scrollPane;

    /**
     * Creates a new dataSet
     *
     * @param dataSet   the dataSet to paint
     * @param modelSync lock to access the model
     */
    public GraphComponent(ValueTable dataSet, Sync modelSync) {
        plotter = new DataPlotter(dataSet).setModelSync(modelSync);
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

        plotter.drawTo(new GraphicSwing(g2), null);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = plotter.getCurrentGraphicWidth();
        if (w < 600) w = 600;
        return new Dimension(w, plotter.getGraphicHeight());
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
        f = plotter.scale(f);
        // keep relative mouse position
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
        plotter.fitInside(width);
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

    /**
     * @return the data plotter
     */
    public DataPlotter getPlotter() {
        return plotter;
    }
}
