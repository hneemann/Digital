/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.Ground;
import de.neemann.digital.core.io.VDD;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;

/**
 *
 * @author ideras
 */
public class ConstVerilog extends VerilogElement {

    /**
     * Creates a new instance
     * @param description the description
     */
    public ConstVerilog(ElementTypeDescription description) {
        super(description);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) {
        Port outPort = node.getPorts().getOutputs().get(0);
        int bits = 0;
        long value = 0;

        if (node.is(Const.DESCRIPTION)) {

            Object obj = node.get(Keys.VALUE);
            bits = node.get(Keys.BITS);

            if (obj instanceof Integer) {
                value = (Integer) obj;
            } else if (obj instanceof Long) {
                value = (Long) obj;
            } else {
                value = 0;
            }
        } else if (node.is(VDD.DESCRIPTION)) {
            value = 1;
            bits = 1;
        } else if (node.is(Ground.DESCRIPTION)) {
            value = 0;
            bits = 1;
        }

        vcBuilder.setCodeIrForSignal(outPort.getSignal().getName(), new VConstExpr(bits, value));
    }

}
