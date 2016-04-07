package de.neemann.digital.gui.state;

/**
 * @author hneemann
 */
public class StateManager {

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

}
