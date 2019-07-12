/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;

/**
 * Represents a node which is build from a circuit.
 */
public class HDLNodeCustom extends HDLNodeBuildIn {
    private final HDLCircuit hdlCircuit;

    /**
     * Creates a new instance
     *
     * @param elementAttributes the attributes
     * @param hdlCircuit        the circuit to use to create this node
     */
    HDLNodeCustom(ElementAttributes elementAttributes, HDLCircuit hdlCircuit) {
        super(hdlCircuit.getElementName(), elementAttributes, hdlCircuit);
        this.hdlCircuit = hdlCircuit;
    }

    /**
     * @return the circuit
     */
    public HDLCircuit getCircuit() {
        return hdlCircuit;
    }

    @Override
    public void setHdlEntityName(String hdlEntityName) {
        throw new RuntimeException("call not supported!");
    }

    @Override
    public String getHdlEntityName() {
        return hdlCircuit.getHdlEntityName();
    }
}
