/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

/**
 * Esxeption thrown if there is a problem delaing with the FSM
 */
public class FiniteStateMachineException extends Exception {
    /**
     * Creates a new exception
     *
     * @param message the message
     */
    public FiniteStateMachineException(String message) {
        super(message);
    }

    /**
     * Creates a new exception
     *
     * @param message the message
     * @param cause   the cause
     */
    public FiniteStateMachineException(String message, Exception cause) {
        super(message, cause);
    }
}
