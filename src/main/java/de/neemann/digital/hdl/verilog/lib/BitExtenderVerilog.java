/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.arithmetic.BitExtender;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VConcatExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VReplicateExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VSliceExpr;

/**
 *
 * @author ideras
 */
public class BitExtenderVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public BitExtenderVerilog() {
        super(BitExtender.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int outBits = node.get(Keys.OUTPUT_BITS);
        int inBits = node.get(Keys.INPUT_BITS);
        int diff = outBits - inBits;

        Port inport = node.getPorts().getInputs().get(0);
        VIRNode inExprNode = vcBuilder.getSignalCodeIr(inport.getSignal());
        VExpr inExpr = inExprNode.resolveToExpr(vcBuilder);

        VExpr replExpr;

        if (inBits > 1)
            replExpr = new VReplicateExpr(diff, new VSliceExpr(inExpr, inBits - 1));
        else
            replExpr = new VReplicateExpr(diff, inExpr);

        Signal outs = node.getPorts().getOutputs().get(0).getSignal();

        vcBuilder.setCodeIrForSignal(outs.getName(), new VConcatExpr(replExpr, inExpr));
    }
}
