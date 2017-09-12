package de.neemann.digital.gui.components;

import java.awt.*;

/**
 * More simple to use GridBagConstrains
 */
public class ConstrainsBuilder extends GridBagConstraints {

    /**
     * Creates a new instance.
     * Position is set to (0,0)
     */
    public ConstrainsBuilder() {
        gridx = 0;
        gridy = 0;
    }

    private ConstrainsBuilder(ConstrainsBuilder original) {
        gridx = original.gridx;
        gridy = original.gridy;
        gridwidth = original.gridwidth;
        gridheight = original.gridheight;
        weightx = original.weightx;
        weighty = original.weighty;
        ipadx = original.ipadx;
        ipady = original.ipady;
        fill = original.fill;
        insets = original.insets;
    }

    /**
     * Sets the position
     *
     * @param x x position
     * @param y y position
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder pos(int x, int y) {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.gridx = x;
        c.gridy = y;
        return c;
    }

    /**
     * Sets the position
     *
     * @param x x position
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder x(int x) {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.gridx = x;
        return c;
    }

    /**
     * Sets the width
     *
     * @param x width
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder width(int x) {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.gridwidth = x;
        return c;
    }

    /**
     * Sets a dynamic height
     *
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder dynamicHeight() {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.weighty = 1;
        return c;
    }

    /**
     * Sets a dynamic width
     *
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder dynamicWidth() {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.weightx = 1;
        return c;
    }


    /**
     * Sets the padding
     *
     * @param x x padding
     * @param y y padding
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder pad(int x, int y) {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.ipadx = x;
        c.ipady = y;
        return c;
    }

    /**
     * Sets the fill attribute to BOTH
     *
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder fill() {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.fill = GridBagConstraints.BOTH;
        return c;
    }

    /**
     * Sets insets to a border of width b
     *
     * @param b border width
     * @return the new created ConstrainsBuilder instance
     */
    public ConstrainsBuilder inset(int b) {
        ConstrainsBuilder c = new ConstrainsBuilder(this);
        c.insets = new Insets(b, b, b, b);
        return c;
    }

    /**
     * Increases the row.
     * Does not create a new instance!
     *
     * @return this for chained calls
     */
    public ConstrainsBuilder nextRow() {
        gridx = 0;
        gridy++;
        return this;
    }

}
