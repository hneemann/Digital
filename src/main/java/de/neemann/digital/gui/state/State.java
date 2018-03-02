/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.state;

import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;

/**
 * A simple state
 */
public class State implements StateInterface {
    private static final Border ENABLED_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private static final Border DISABLED_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private JComponent indicator;
    private StateManager stateManager;
    private ToolTipAction action;

    /**
     * Creates new state
     */
    public State() {
    }

    /**
     * The JComponent used to indicate the state
     *
     * @param indicator the JComponent
     * @param <C>       the type of the JComponent
     * @return the JComponent for call chaining
     */
    public <C extends JComponent> C setIndicator(C indicator) {
        this.indicator = indicator;
        indicator.setBorder(DISABLED_BORDER);
        return indicator;
    }

    void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    /**
     * Sets the state indicator to "activated"
     */
    public void enter() {
        stateManager.leaveActualStateAndSet(this);
        if (indicator != null)
            indicator.setBorder(ENABLED_BORDER);
    }

    @Override
    public void leave() {
        if (indicator != null)
            indicator.setBorder(DISABLED_BORDER);
    }

    /**
     * @return the action associated with this state
     */
    public ToolTipAction getAction() {
        return action;
    }

    /**
     * Creates a tooltip action which activates the state
     *
     * @param name the name of the action to create
     * @param icon the icon to use
     * @return the action
     */
    public ToolTipAction createToolTipAction(String name, Icon icon) {
        if (action == null)
            action = new ToolTipAction(name, icon) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enter();
                }
            };
        return action;
    }

    /**
     * @return true if this state is active
     */
    public boolean isActive() {
        return stateManager.isActive(this);
    }
}
