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

        dataSet.drawTo(new GraphicSwing(g2), false);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = dataSet.getGraphicWidth();
        if (w < 800) w = 800;
        return new Dimension(w, dataSet.getGraphicHeight());
    }
}
