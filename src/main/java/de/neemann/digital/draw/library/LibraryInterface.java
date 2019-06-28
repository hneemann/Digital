/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * Library interface used by the model creator
 */
public interface LibraryInterface {
    /**
     * Creates a element description.
     *
     * @param elementName the name of the element
     * @param attr        the elements attributes
     * @return the ElementTypeDescription
     * @throws ElementNotFoundException ElementNotFoundException
     */
    ElementTypeDescription getElementType(String elementName, ElementAttributes attr) throws ElementNotFoundException;
}
