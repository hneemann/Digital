/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VBinaryExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;

/**
 *
 * @author ideras
 */
public class OperateVerilog extends VerilogElement {
    private final VOperator oper;

    /**
     * Creates a new instance
     *
     * @param oper        the operator
     * @param description the elements description
     */
    public OperateVerilog(VOperator oper, ElementTypeDescription description) {
        super(description);
        this.oper = oper;
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        VExpr resultExpr = null;

        for (Port p : node.getPorts().getInputs()) {
            VIRNode inCodeIr = vcBuilder.getSignalCodeIr(p.getSignal());
            VExpr inExpr = inCodeIr.resolveToExpr(vcBuilder);

            if (resultExpr == null) {
                resultExpr = inExpr;
            } else {
                resultExpr = new VBinaryExpr(resultExpr, inExpr, oper);
            }
        }

        Signal outSignal = node.getPorts().get(0).getSignal();

        resultExpr.setSignal(outSignal);
        vcBuilder.setCodeIrForSignal(outSignal, resultExpr);
    }
}
