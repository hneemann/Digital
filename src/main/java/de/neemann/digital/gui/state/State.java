package de.neemann.digital.gui.state;

import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;

/**
 * @author hneemann
 */
public class State implements StateInterface {
    private static final Border enabledBorder = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private static final Border disabledBorder = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private JComponent indicator;
    private StateManager stateManager;

    public State() {
    }

    public <C extends JComponent> C setIndicator(C indicator) {
        this.indicator = indicator;
        indicator.setBorder(disabledBorder);
        return indicator;
    }

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public void enter() {
        if (indicator != null)
            indicator.setBorder(enabledBorder);
    }

    @Override
    public void leave() {
        if (indicator != null)
            indicator.setBorder(disabledBorder);
    }

    public ToolTipAction createToolTipAction(String name, Icon icon) {
        return new ToolTipAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                stateManager.setState(State.this);
            }
        };
    }

    public void activate() {
        stateManager.setState(this);
    }


    public void indicate() {
        if (indicator != null)
            indicator.setBorder(enabledBorder);
    }
}
