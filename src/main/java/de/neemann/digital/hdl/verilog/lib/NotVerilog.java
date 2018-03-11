/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.basic.Not;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.expr.VUnaryExpr;

/**
 *
 * @author ideras
 */
public class NotVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public NotVerilog() {
        super(Not.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        Port inport = node.getPorts().getInputs().get(0);
        Port outport = node.getPorts().getOutputs().get(0);
        VIRNode inIrNode = vcBuilder.getSignalCodeIr(inport.getSignal());
        VExpr inExpr = inIrNode.resolveToExpr(vcBuilder);

        Signal outs = outport.getSignal();
        VExpr resultExpr = new VUnaryExpr(inExpr, VOperator.NOT);
        resultExpr.setSignal(outs);
        vcBuilder.setCodeIrForSignal(outs, resultExpr);
    }

}
