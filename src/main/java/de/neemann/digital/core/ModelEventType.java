/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * A event fired by the model
 */
public enum ModelEventType {

    /**
     * Is fired after the model had became stable after first stabilization.
     */
    STARTED,

    /**
     * The model has stopped.
     */
    CLOSED,

    /**
     * The model has stopped and the CLOSED event was fired.
     */
    POSTCLOSED,

    /**
     * Is fired if the model had performed a full step.
     * This means a change is propagated through all nodes, and the model has
     * become stable again.
     */
    STEP,

    /**
     * Fast run is started.
     */
    RUN_TO_BREAK,

    /**
     * Fast run timeout has occurred.
     */
    RUN_TO_BREAK_TIMEOUT,

    /**
     * A break is detected.
     */
    BREAK,

    /**
     * Is fired if a micro step is calculated.
     * This means the aktual nodes are calculated, but not the effected nodes.
     */
    MICROSTEP,

    /**
     * Is fired in case a burn check needs to be done without the change of a gate.
     * Can happen if inputs are connected directly.
     */
    CHECKBURN,

    /**
     * Used to notify an error
     */
    ERROR_OCCURRED
}
