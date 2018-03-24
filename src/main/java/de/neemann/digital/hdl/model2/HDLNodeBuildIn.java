/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;

/**
 * A node which represents a build-in component
 */
public class HDLNodeBuildIn extends HDLNode {
    /**
     * Creates e new instance
     *
     * @param elementName       the instances name
     * @param elementAttributes the attributes
     * @param bitProvider       the bit provider which provides the outputs bit width
     */
    HDLNodeBuildIn(String elementName, ElementAttributes elementAttributes, HDLModel.BitProvider bitProvider) {
        super(elementName, elementAttributes, bitProvider);
    }
}
