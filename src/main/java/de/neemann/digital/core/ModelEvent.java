/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import java.util.Objects;

/**
 * a model event
 */
public class ModelEvent {
    /**
     * Shorthand for a ModelEventType.STARTED event
     */
    public static final ModelEvent STARTED = new ModelEvent(ModelEventType.STARTED);
    /**
     * Shorthand for a ModelEventType.STOPPED event
     */
    public static final ModelEvent CLOSED = new ModelEvent(ModelEventType.CLOSED);
    /**
     * Shorthand for a ModelEventType.STEP event
     */
    public static final ModelEvent STEP = new ModelEvent(ModelEventType.STEP);
    /**
     * Shorthand for a ModelEventType.MICROSTEP event
     */
    public static final ModelEvent MICROSTEP = new ModelEvent(ModelEventType.MICROSTEP);
    /**
     * Shorthand for a ModelEventType.FASTRUN event
     */
    public static final ModelEvent FASTRUN = new ModelEvent(ModelEventType.FASTRUN);
    /**
     * Shorthand for a ModelEventType.BREAK event
     */
    public static final ModelEvent BREAK = new ModelEvent(ModelEventType.BREAK);
    /**
     * Shorthand for a ModelEventType.EXTERNALCHANGE event
     */
    public static final ModelEvent EXTERNALCHANGE = new ModelEvent(ModelEventType.EXTERNALCHANGE);

    private final ModelEventType type;
    private Exception cause;

    private ModelEvent(ModelEventType type) {
        this.type = type;
    }

    ModelEvent(Exception cause) {
        this(ModelEventType.ERROR_OCCURRED);
        this.cause = cause;
    }

    /**
     * @return the event type
     */
    public ModelEventType getType() {
        return type;
    }

    /**
     * @return the cause in case of an error
     */
    public Exception getCause() {
        return cause;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelEvent that = (ModelEvent) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
