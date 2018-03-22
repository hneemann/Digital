/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;

public class HDLNodeCustom extends HDLNode {
    private final HDLCircuit hdlCircuit;

    public HDLNodeCustom(String elementName, ElementAttributes elementAttributes, HDLCircuit hdlCircuit) {
        super(elementName, elementAttributes, hdlCircuit);
        this.hdlCircuit = hdlCircuit;
    }

    @Override
    public void traverse(HDLVisitor visitor) {
        hdlCircuit.traverse(visitor);
        visitor.visit(this);
    }
}
