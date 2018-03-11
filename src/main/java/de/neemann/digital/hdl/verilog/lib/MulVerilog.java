/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.arithmetic.Mul;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VBinaryExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;

/**
 *
 * @author ideras
 */
public class MulVerilog extends VerilogElement {

    /**
     * Initialize a new instance
     */
    public MulVerilog() {
        super(Mul.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        Port inpa = node.getPorts().getInputs().get(0);
        Port inpb = node.getPorts().getInputs().get(1);
        Port outr = node.getPorts().getOutputs().get(0);
        VIRNode na = vcBuilder.getSignalCodeIr(inpa.getSignal());
        VIRNode nb = vcBuilder.getSignalCodeIr(inpb.getSignal());
        VExpr expra = na.resolveToExpr(vcBuilder);
        VExpr exprb = nb.resolveToExpr(vcBuilder);
        VExpr resultExpr;

        resultExpr = new VBinaryExpr(expra, exprb, VOperator.MUL);
        resultExpr.setSignal(outr.getSignal());
        vcBuilder.setCodeIrForSignal(outr.getSignal(), resultExpr);
    }

}
