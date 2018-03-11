/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.state;

/**
 * Organizes the state switches.
 * Simply holds the actual state, so that if can be disabled by a new state.
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

    /**
     * Returns true if the given state is the active state
     *
     * @param state the state
     * @return true if the given state is active
     */
    public boolean isActive(State state) {
        return actualState == state;
    }

    /**
     * Sets the actual state. Ony used to init the state manager
     *
     * @param active the state which is active.
     */
    public void setActualState(State active) {
        this.actualState = active;
    }
}
