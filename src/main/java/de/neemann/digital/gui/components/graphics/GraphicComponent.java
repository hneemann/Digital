package de.neemann.digital.gui.components.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * @author hneemann
 */
public class GraphicComponent extends JComponent {

    private final int pixSize;
    private final int width;
    private final int height;
    private long[] data;
    private int offs;

    public GraphicComponent(int width, int height) {
        this.width = width;
        this.height = height;
        pixSize=4;
        Dimension size = new Dimension(width * pixSize, height * pixSize);
        setPreferredSize(size);
        setMinimumSize(size);
        setMinimumSize(size);
    }

    public void updateGraphic(long[] data, boolean bank) {
        this.data = data;
        if (bank)
            offs=width*height;
        else
            offs=0;
        repaint();
    }

    @Override
    protected void printComponent(Graphics g) {
        super.printComponent(g);
    }
}
