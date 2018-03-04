/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

/**
 * Interface is used to implement a factory for elements
 */
public interface ElementFactory {
    /**
     * creates a new element matching the given attributes
     *
     * @param attributes the attributes describing the element
     * @return the created element
     */
    Element create(ElementAttributes attributes);
}
