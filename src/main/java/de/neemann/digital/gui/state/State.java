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
    private static final Border ENABLED_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private static final Border DISABLED_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private JComponent indicator;
    private StateManager stateManager;

    public State() {
    }

    public <C extends JComponent> C setIndicator(C indicator) {
        this.indicator = indicator;
        indicator.setBorder(DISABLED_BORDER);
        return indicator;
    }

    public void setStateManager(StateManager stateManager) {
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

}
