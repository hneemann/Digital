/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;

import java.util.ArrayList;

public class HDLNode {
    private final String elementName;
    private final ElementAttributes elementAttributes;
    private final HDLContext.BitProvider bitProvider;
    private final ArrayList<HDLPort> inputs;
    private final ArrayList<HDLPort> outputs;

    public HDLNode(String elementName, ElementAttributes elementAttributes, HDLContext.BitProvider bitProvider) {
        this.elementName = elementName;
        this.elementAttributes = elementAttributes;
        this.bitProvider = bitProvider;
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public void addInput(HDLPort port) {
        inputs.add(port);
        port.setNode(this);
    }

    public void addOutput(HDLPort port) {
        outputs.add(port);
        port.setNode(this);
    }

    @Override
    public String toString() {
        return elementName + " " + inputs + " " + outputs;
    }

    public String getElementName() {
        return elementName;
    }

    public ElementAttributes getElementAttributes() {
        return elementAttributes;
    }

    public ArrayList<HDLPort> getInputs() {
        return inputs;
    }

    public ArrayList<HDLPort> getOutputs() {
        return outputs;
    }

    public void traverse(HDLVisitor visitor) {
        visitor.visit(this);
    }

    public int getBits(String name) {
        return bitProvider.getBits(name);
    }
}
