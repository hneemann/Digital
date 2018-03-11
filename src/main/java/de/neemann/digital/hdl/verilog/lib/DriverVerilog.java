/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Driver;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.stmt.VIfStatement;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;
import de.neemann.digital.hdl.verilog.ir.expr.VUnaryExpr;


/**
 * the driver VHDL entity
 */
public class DriverVerilog extends VerilogElement {
    private final boolean invert;

    /**
     * creates a new instance
     *
     * @param invert true if inverted input
     */
    public DriverVerilog(boolean invert) {
        super(Driver.DESCRIPTION);
        this.invert = invert;
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int bits = node.get(Keys.BITS);
        Port inp = node.getPorts().getInputs().get(0);
        Port selp = node.getPorts().getInputs().get(1);
        Port outp = node.getPorts().getOutputs().get(0);
        Signal outs = outp.getSignal();

        VIRNode inExprNode = vcBuilder.getSignalCodeIr(inp.getSignal());
        VIRNode selExprNode = vcBuilder.getSignalCodeIr(selp.getSignal());
        VExpr inExpr = inExprNode.resolveToExpr(vcBuilder);
        VExpr selExpr = selExprNode.resolveToExpr(vcBuilder);

        VStatementPlace place = new VStatementPlace(outs);
        vcBuilder.registerSignalDecl(outs, VSignalDecl.Type.REG);

        if (invert) {
            selExpr = new VUnaryExpr(selExpr, VOperator.NOT);
        }

        VStatement stmt = new VIfStatement(selExpr,
                                           new VAssignStatement(place, inExpr),
                                           new VAssignStatement(place, new VConstExpr(bits, VConstExpr.Type.Z)));
        stmt.setPlace(place);

        vcBuilder.setCodeIrForSignal(outp.getSignal(), stmt);
    }
}
