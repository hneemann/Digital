package de.neemann.digital.gui.components;

import java.awt.*;

/**
 * More simple to use GridBagConstrains
 */
public class GBC extends GridBagConstraints {

    /**
     * Creates a new instance.
     * Position is set to (0,0)
     */
    public GBC() {
        gridx = 0;
        gridy = 0;
    }

    private GBC copy() {
        GBC n = new GBC();
        n.gridx = gridx;
        n.gridy = gridy;
        n.gridwidth = gridwidth;
        n.gridheight = gridheight;
        n.weightx = weightx;
        n.weighty = weighty;
        n.ipadx = ipadx;
        n.ipady = ipady;
        n.fill = fill;
        n.insets = insets;
        return n;
    }

    /**
     * Sets the position
     *
     * @param x x position
     * @param y y position
     * @return the new created GBC instance
     */
    public GBC pos(int x, int y) {
        GBC c = copy();
        c.gridx = x;
        c.gridy = y;
        return c;
    }

    /**
     * Sets the position
     *
     * @param x x position
     * @return the new created GBC instance
     */
    public GBC x(int x) {
        GBC c = copy();
        c.gridx = x;
        return c;
    }

    /**
     * Sets the width
     *
     * @param x width
     * @return the new created GBC instance
     */
    public GBC width(int x) {
        GBC c = copy();
        c.gridwidth = x;
        return c;
    }

    /**
     * Sets a dynamic height
     *
     * @return the new created GBC instance
     */
    public GBC dynamicHeight() {
        GBC c = copy();
        c.weighty = 1;
        return c;
    }

    /**
     * Sets a dynamic width
     *
     * @return the new created GBC instance
     */
    public GBC dynamicWidth() {
        GBC c = copy();
        c.weightx = 1;
        return c;
    }


    /**
     * Sets the padding
     *
     * @param x x padding
     * @param y y padding
     * @return the new created GBC instance
     */
    public GBC pad(int x, int y) {
        GBC c = copy();
        c.ipadx = x;
        c.ipady = y;
        return c;
    }

    /**
     * Sets the fill attribute to BOTH
     *
     * @return the new created GBC instance
     */
    public GBC fill() {
        GBC c = copy();
        c.fill = GridBagConstraints.BOTH;
        return c;
    }

    /**
     * Sets insets to a border of width b
     *
     * @param b border width
     * @return the new created GBC instance
     */
    public GBC inset(int b) {
        GBC c = copy();
        c.insets = new Insets(b, b, b, b);
        return c;
    }

    /**
     * Increases the row.
     * Does not create a new instance
     *
     * @return this for chained calls
     */
    public GBC nextRow() {
        gridx = 0;
        gridy++;
        return this;
    }

}
