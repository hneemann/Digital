package de.neemann.digital.gui.state;

import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;

/**
 * A simple state
 *
 * @author hneemann
 */
public class State implements StateInterface {
    private static final Border ENABLED_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private static final Border DISABLED_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private JComponent indicator;
    private StateManager stateManager;

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

    @Override
    public void enter() {
        if (indicator != null)
            indicator.setBorder(ENABLED_BORDER);
    }

    @Override
    public void leave() {
        if (indicator != null)
            indicator.setBorder(DISABLED_BORDER);
    }

    /**
     * Creates a tooltip action which activates the state
     *
     * @param name the name of the action to create
     * @param icon the icon to use
     * @return the acttion
     */
    public ToolTipAction createToolTipAction(String name, Icon icon) {
        return new ToolTipAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                stateManager.setState(State.this);
            }
        };
    }

    /**
     * Activates this state
     */
    public void activate() {
        stateManager.setState(this);
    }

}
