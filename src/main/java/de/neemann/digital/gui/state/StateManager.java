package de.neemann.digital.gui.state;

/**
 * Organizes the state switches
 *
 * @author hneemann
 */
public class StateManager {

    private StateInterface actualState;

    /**
     * Creates a new instance
     */
    public StateManager() {
    }

    /**
     * Activates the given state
     *
     * @param state the state to activate
     */
    public void setState(StateInterface state) {
        if (actualState != null)
            actualState.leave();
        actualState = state;
        if (actualState != null)
            actualState.enter();
    }

    /**
     * Registers a state to this manager
     *
     * @param state the state to register
     * @param <T>   the type of the state
     * @return this for call chaining
     */
    public <T extends State> T register(T state) {
        state.setStateManager(this);
        return state;
    }

}
