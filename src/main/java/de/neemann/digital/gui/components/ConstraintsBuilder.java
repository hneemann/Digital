/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import java.awt.*;

/**
 * More simple to use GridBagConstraints
 */
public class ConstraintsBuilder extends GridBagConstraints {

    /**
     * Creates a new instance.
     * Position is set to (0,0)
     */
    public ConstraintsBuilder() {
        gridx = 0;
        gridy = 0;
    }

    private ConstraintsBuilder(ConstraintsBuilder original) {
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
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder pos(int x, int y) {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.gridx = x;
        c.gridy = y;
        return c;
    }

    /**
     * Sets the position
     *
     * @param x x position
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder x(int x) {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.gridx = x;
        return c;
    }

    /**
     * Sets the width
     *
     * @param x width
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder width(int x) {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.gridwidth = x;
        return c;
    }

    /**
     * Sets a dynamic height
     *
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder dynamicHeight() {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.weighty = 1;
        return c;
    }

    /**
     * Sets a dynamic width
     *
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder dynamicWidth() {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.weightx = 1;
        return c;
    }


    /**
     * Sets the padding
     *
     * @param x x padding
     * @param y y padding
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder pad(int x, int y) {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.ipadx = x;
        c.ipady = y;
        return c;
    }

    /**
     * Sets the fill attribute to BOTH
     *
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder fill() {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.fill = GridBagConstraints.BOTH;
        return c;
    }

    /**
     * Sets insets to a border of width b
     *
     * @param b border width
     * @return the new created ConstraintsBuilder instance
     */
    public ConstraintsBuilder inset(int b) {
        ConstraintsBuilder c = new ConstraintsBuilder(this);
        c.insets = new Insets(b, b, b, b);
        return c;
    }

    /**
     * Increases the row.
     * Does not create a new instance!
     *
     * @return this for chained calls
     */
    public ConstraintsBuilder nextRow() {
        gridx = 0;
        gridy++;
        return this;
    }

}
