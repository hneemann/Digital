/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.BitSelector;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VSliceExpr;

/**
 *
 * @author ideras
 */
public class BitSelectorVerilog extends VerilogElement {

    /**
     * Initialize a new instance
     */
    public BitSelectorVerilog() {
        super(BitSelector.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int selBits = node.get(Keys.SELECTOR_BITS);
        int dataBits = 1 << selBits;

        Port inp = node.getPorts().getInputs().get(0);
        Port selp = node.getPorts().getInputs().get(1);
        VIRNode inExprNode = vcBuilder.getSignalCodeIr(inp.getSignal());
        VIRNode selExprNode = vcBuilder.getSignalCodeIr(selp.getSignal());
        VExpr inExpr = inExprNode.resolveToExpr(vcBuilder);
        VExpr selExpr = selExprNode.resolveToExpr(vcBuilder);
        selExpr = selExpr.resolveToIdExpr(vcBuilder);

        VExpr expr = new VSliceExpr(inExpr, selExpr);
        Signal outs = node.getPorts().getOutputs().get(0).getSignal();

        vcBuilder.setCodeIrForSignal(outs.getName(), expr);
    }

}
