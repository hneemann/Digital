/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Is thrown if more than one output of a set of connected outputs becomes active
 */
public class BurnException extends RuntimeException implements ExceptionWithOriginInterface {
    private final ObservableValues values;
    private Set<File> origin;

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param values  the values connected to the net
     */
    public BurnException(String message, ObservableValues values) {
        super(message);
        this.values = values;
    }

    /**
     * Adds a file to the origin of this exception
     *
     * @param file the origin
     * @return this for chained calls
     */
    public BurnException addOrigin(File file) {
        if (origin == null)
            origin = new HashSet<File>();
        origin.add(file);
        return this;
    }

    /**
     * Adds a set of files file to the origin of this exception
     *
     * @param file the origin
     * @return this for chained calls
     */
    public BurnException addOrigin(Set<File> file) {
        if (origin == null)
            origin = new HashSet<File>();
        origin.addAll(file);
        return this;
    }

    /**
     * @return returns the causing value
     */
    public ObservableValues getValues() {
        return values;
    }

    @Override
    public Set<File> getOrigin() {
        return origin;
    }
}
