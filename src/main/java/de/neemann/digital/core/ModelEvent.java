package de.neemann.digital.core;

/**
 * A event fired by the model
 *
 * @author hneemann
 */
public enum ModelEvent {

    /**
     * Is fired after the model had became stable after first stabilization.
     */
    STARTED,

    /**
     * The model had stopped.
     */
    STOPPED,

    /**
     * Is fired if the model had performed a full step.
     * This means a change is propagated through all nodes, and the model has
     * become stable again.
     */
    STEP,

    /**
     * Fast run is started.
     */
    FASTRUN,

    /**
     * A break is detected.
     */
    BREAK,

    /**
     * Here was a manual change to the model by the user.
     */
    MANUALCHANGE,

    /**
     * If fired if a micro step is calculated.
     * This means the aktual nodes are calculated, but not the effected nodes.
     */
    MICROSTEP

}
