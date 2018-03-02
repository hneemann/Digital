/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

/**
 * Exception thrown if e element does't exist
 * <p/>
 * Created by helmut.neemann on 08.11.2016.
 */
public class ElementNotFoundException extends Exception {
    /**
     * Creates a new Instance
     *
     * @param message the error message
     */
    public ElementNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new Instance
     *
     * @param message the error message
     * @param cause   the errors cause
     */
    public ElementNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
