/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.expr.VBinaryExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.expr.VConcatExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;

/**
 *
 * @author ideras
 */
public class AddVerilog extends VerilogElement {
    private final VOperator oper;

    /**
     * Creates a new instance
     * @param description the description
     * @param oper the operator
     */
    public AddVerilog(ElementTypeDescription description, VOperator oper) {
        super(description);
        this.oper = oper;
    }

    /**
     * Returns the operator
     *
     * @return the operator
     */
    public VOperator getOper() {
        return oper;
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        Port inporta = node.getPorts().getInputs().get(0);
        Port inportb = node.getPorts().getInputs().get(1);
        Port inportc = node.getPorts().getInputs().get(2);
        VIRNode inCodeIrA = vcBuilder.getSignalCodeIr(inporta.getSignal());
        VIRNode inCodeIrB = vcBuilder.getSignalCodeIr(inportb.getSignal());
        VIRNode inCodeIrC = vcBuilder.getSignalCodeIr(inportc.getSignal());
        VExpr inExprA = inCodeIrA.resolveToExpr(vcBuilder);
        VExpr inExprB = inCodeIrB.resolveToExpr(vcBuilder);
        VExpr inExprC = inCodeIrC.resolveToExpr(vcBuilder);
        Signal outSum = node.getPorts().getOutputs().get(0).getSignal();
        Signal outCO = node.getPorts().getOutputs().get(1).getSignal();

        if (outCO != null) {
            inExprA = new VConcatExpr(new VConstExpr(1, 0), inExprA);
            inExprB = new VConcatExpr(new VConstExpr(1, 0), inExprB);
        }

        VExpr resultExpr;

        if (inExprC instanceof VConstExpr) {
            VConstExpr cexpr = (VConstExpr) inExprC;

            if (cexpr.getValue() == 0) {
                resultExpr = new VBinaryExpr(inExprA, inExprB, oper);
            } else {
                resultExpr = new VBinaryExpr(inExprA, new VBinaryExpr(inExprB, inExprC, oper), oper);
            }
        } else {
            resultExpr = new VBinaryExpr(inExprA, new VBinaryExpr(inExprB, inExprC, oper), oper);
        }

        int bits = node.get(Keys.BITS);

        if (outSum != null && outCO != null) {
            VStatementPlace place = new VStatementPlace(outCO, outSum);
            VStatement assignStmt = new VAssignStatement(place, resultExpr);

            vcBuilder.registerSignalDecl(outSum, VSignalDecl.Type.WIRE);
            vcBuilder.registerSignalDecl(outCO, VSignalDecl.Type.WIRE);
            assignStmt.resolveToExpr(vcBuilder);
        } else {
            if (outSum != null) {
                resultExpr.setSignal(outSum);
                vcBuilder.setCodeIrForSignal(outSum.getName(), resultExpr);
            }

            if (outCO != null) {
                resultExpr = new VBinaryExpr(resultExpr, new VConstExpr(32, bits), VOperator.SHR)
                              .setSignal(outCO);

                vcBuilder.setCodeIrForSignal(outCO, resultExpr);
            }
        }
    }

}
