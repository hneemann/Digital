/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VConcatExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VSliceExpr;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class SplitterVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public SplitterVerilog() {
        super(Splitter.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        if (node.getPorts().getInputs().size() == 1) {
            Signal inSignal = node.getPorts().getInputs().get(0).getSignal();
            VIRNode inCodeIr = vcBuilder.getSignalCodeIr(inSignal);
            VExpr inExpr = inCodeIr.resolveToExpr(vcBuilder);
            inExpr = inExpr.resolveToIdExpr(vcBuilder);

            for (Port p : node.getPorts().getOutputs()) {
                String portName = p.getOrigName();

                if (p.getSignal() == null) {
                    continue;
                }

                String[] strLimits = portName.split("[-,]");
                VExpr expr;

                if (strLimits.length == 2) {
                    int start = Integer.parseInt(strLimits[1]);
                    int end = Integer.parseInt(strLimits[0]);

                    expr = new VSliceExpr(inExpr, start, end);
                } else {
                    int index = Integer.parseInt(strLimits[0]);
                    expr = new VSliceExpr(inExpr, index);
                }

                expr.setSignal(p.getSignal());
                vcBuilder.setCodeIrForSignal(p.getSignal(), expr);
            }
        } else { // Assumes one output signal
            Port outPort = node.getPorts().getOutputs().get(0);
            Signal outSignal = outPort.getSignal();

            ArrayList<VExpr> exprList = new ArrayList<>();

            for (Port p : node.getPorts().getInputs()) {
                VIRNode inCodeIrNode = vcBuilder.getSignalCodeIr(p.getSignal());
                VExpr inExpr = inCodeIrNode.resolveToExpr(vcBuilder);

                exprList.add(0, inExpr);
            }
            VExpr expr = new VConcatExpr(exprList);

            expr.setSignal(outSignal);
            vcBuilder.setCodeIrForSignal(outSignal, expr);
        }
    }

}
