/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.arithmetic.Comparator;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VBinaryExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.expr.VSignedExpr;

/**
 *
 * @author ideras
 */
public class ComparatorVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public ComparatorVerilog() {
        super(Comparator.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        Port inporta = node.getPorts().getInputs().get(0);
        Port inportb = node.getPorts().getInputs().get(1);
        Signal insignala = inporta.getSignal();
        Signal insignalb = inportb.getSignal();
        Port outgt = node.getPorts().getOutputs().get(0);
        Port outeq = node.getPorts().getOutputs().get(1);
        Port outlt = node.getPorts().getOutputs().get(2);

        VIRNode codeIr1 = vcBuilder.getSignalCodeIr(insignala);
        VIRNode codeIr2 = vcBuilder.getSignalCodeIr(insignalb);
        VExpr expr1 = codeIr1.resolveToExpr(vcBuilder);
        VExpr expr2 = codeIr2.resolveToExpr(vcBuilder);

        if (node.get(Keys.SIGNED)) {
            expr1 = new VSignedExpr(expr1);
            expr2 = new VSignedExpr(expr2);
        }

        if (outgt.getSignal() != null) {
            VExpr expr = new VBinaryExpr(expr1, expr2, VOperator.GT);
            vcBuilder.setCodeIrForSignal(outgt.getSignal().getName(), expr);
        }
        if (outeq.getSignal() != null) {
            VExpr expr = new VBinaryExpr(expr1, expr2, VOperator.EQ);
            vcBuilder.setCodeIrForSignal(outeq.getSignal().getName(), expr);
        }
        if (outlt.getSignal() != null) {
            VExpr expr = new VBinaryExpr(expr1, expr2, VOperator.LT);
            vcBuilder.setCodeIrForSignal(outlt.getSignal().getName(), expr);
        }
    }
}
