package de.neemann.digital.gui.state;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * @author hneemann
 */
public class StateManager {
    private static final Border enabledBorder = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4));
    private static final Border disabledBorder = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(4, 4, 4, 4));

    private StateInterface actualState;

    public StateManager() {
    }

    public void setState(StateInterface state) {
        if (actualState != null)
            actualState.leave();
        actualState = state;
        if (actualState != null)
            actualState.enter();
    }

    public <T extends State> T register(T state) {
        state.setStateManager(this);
        return state;
    }

    public void stateEnteredManually(State state) {
        if (actualState != null)
            actualState.leave();
        actualState = state;
        if (actualState instanceof State)
            ((State) actualState).indicate();

    }
}
