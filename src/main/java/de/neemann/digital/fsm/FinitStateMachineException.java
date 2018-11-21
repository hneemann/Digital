/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

/**
 * Esxeption thrown if there is a problem delaing with the FSM
 */
public class FinitStateMachineException extends Exception {
    /**
     * Creates a new exception
     *
     * @param message ther message
     */
    public FinitStateMachineException(String message) {
        super(message);
    }
}
