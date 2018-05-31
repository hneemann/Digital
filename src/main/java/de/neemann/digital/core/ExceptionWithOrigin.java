/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.draw.elements.VisualElement;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * A exception which has a set of files as an origin.
 */
public class ExceptionWithOrigin extends Exception implements ExceptionWithOriginInterface {
    private File origin;
    private VisualElement visualElement;

    /**
     * Returns the file or the files that caused the given exception.
     * If no origin is found null is returned.
     *
     * @param e the exception
     * @return the origin or null;
     */
    public static String getOriginOf(Throwable e) {
        while (e != null) {
            if (e instanceof ExceptionWithOriginInterface) {
                Set<File> origins = ((ExceptionWithOriginInterface) e).getOrigin();
                if (origins != null && origins.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (File o : origins) {
                        if (o != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(o.getName());
                        }
                    }
                    if (sb.length() > 0)
                        return sb.toString();
                    else
                        return null;
                }
            }
            e = e.getCause();
        }
        return null;
    }

    /**
     * Creates a new exception
     *
     * @param message message
     */
    public ExceptionWithOrigin(String message) {
        super(message);
    }

    /**
     * Creates a new exception
     *
     * @param message message
     * @param cause   the cause
     */
    public ExceptionWithOrigin(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return the origin of the error
     */
    public Set<File> getOrigin() {
        if (origin == null)
            return null;
        else {
            HashSet<File> os = new HashSet<>();
            os.add(origin);
            return os;
        }
    }

    /**
     * Sets the origin of an error
     *
     * @param origin the file which had caused the exception
     */
    public void setOrigin(File origin) {
        if (origin != null) {
            if (this.origin == null)
                this.origin = origin;
        }
    }

    /**
     * Sets the visual element which caused this error
     *
     * @param visualElement the visual element
     * @return this for chained calls
     */
    public ExceptionWithOrigin setVisualElement(VisualElement visualElement) {
        this.visualElement = visualElement;
        return this;
    }

    /**
     * @return the visual element which caused this error
     */
    public VisualElement getVisualElement() {
        return visualElement;
    }
}
