/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;

/**
 * Responsible of generating the Verilog code.
 *
 * @author ideras
 */
public abstract class VerilogElement {
    private final ElementTypeDescription description;

    /**
     * Verilog element base constructor
     *
     * @param description the description
     */
    public VerilogElement(ElementTypeDescription description) {
        this.description = null;
    }

    /**
     * Returns the associated ElementTypeDescription
     *
     * @return The element description
     */
    public ElementTypeDescription getDescription() {
        return description;
    }

    /**
     * Builds an intermediate representation of the generated Verilog code.
     *
     * @param vcBuilder the verilog code builder instance.
     * @param node      the visiting node.
     * @throws HDLException HDLException
     */
    public abstract void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException;
}
