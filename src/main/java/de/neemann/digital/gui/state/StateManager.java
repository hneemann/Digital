package de.neemann.digital.gui.state;

/**
 * Organizes the state switches.
 * Simply holds the actual state, so that if can be disabled by a new state.
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
    void leaveActualStateAndSet(StateInterface state) {
        if (actualState != null)
            actualState.leave();
        actualState = state;
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
