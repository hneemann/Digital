/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;

/**
 * Panel used in the editor
 */
public class EditorPanel {
    /**
     * The Id for the primary panel
     */
    public static final String PRIMARY = "primary";
    /**
     * The Id for the secondary panel
     */
    public static final String SECONDARY = "secondary";

    private final JPanel panel;
    private final ConstraintsBuilder constraints;
    private final String id;

    /**
     * Creates a new instance
     *
     * @param id the panels id, used to identify the panel and as part of the language key
     */
    public EditorPanel(String id) {
        this.id = id;
        panel = new JPanel(new GridBagLayout());
        constraints = new ConstraintsBuilder().inset(3).fill();
    }

    /**
     * Moves to the next row
     */
    public void nextRow() {
        constraints.nextRow();
    }

    /**
     * @return this panel wrapped with a scroll pane
     */
    public Component getScrollPane() {
        return new JScrollPane(panel);
    }

    /**
     * @return the language key
     */
    public String getLangKey() {
        return "attr_panel_" + id;
    }

    /**
     * Adds a component using the default constrains
     *
     * @param component the components to add
     */
    public void add(JComponent component) {
        panel.add(component, constraints);
    }

    /**
     * Adds a component
     *
     * @param component the components to add
     * @param c         allows to modify the constraints
     */
    public void add(JComponent component, Constraints c) {
        panel.add(component, c.create(constraints));
    }

    /**
     * Adds a button
     *
     * @param label  the label to use
     * @param action the action to use
     */
    public void addButton(String label, ToolTipAction action) {
        panel.add(new JLabel(label), constraints);
        panel.add(action.createJButton(), constraints.x(1));
        constraints.nextRow();
    }

    /**
     * @return the panels id
     */
    public String getPanelId() {
        return id;
    }

    /**
     * The interface used to modify the constraints
     */
    interface Constraints {
        /**
         * Allows to modify the constraints
         *
         * @param cb the default constraints
         * @return the modified constraints
         */
        ConstraintsBuilder create(ConstraintsBuilder cb);
    }
}

