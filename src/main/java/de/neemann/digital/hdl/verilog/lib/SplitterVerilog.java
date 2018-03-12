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
        VExpr inExpr;

        if (node.getPorts().getInputs().size() != 1) {
            ArrayList<VExpr> exprList = new ArrayList<>();

            int totalBits = 0;
            for (Port p : node.getPorts().getInputs()) {
                VIRNode inCodeIrNode = vcBuilder.getSignalCodeIr(p.getSignal());

                exprList.add(0, inCodeIrNode.resolveToExpr(vcBuilder));
                totalBits += p.getBits();
            }
            Signal s = vcBuilder.getModel().createSignal().setBits(totalBits);
            inExpr = new VConcatExpr(exprList).setSignal(s);
        } else {
            Signal inSignal = node.getPorts().getInputs().get(0).getSignal();
            VIRNode inCodeIr = vcBuilder.getSignalCodeIr(inSignal);
            inExpr = inCodeIr.resolveToExpr(vcBuilder);
        }

        if (node.getPorts().getOutputs().size() == 1
            && node.getPorts().getOutputs().get(0).getBits() == inExpr.getSignal().getBits()) {
            Port outPort = node.getPorts().getOutputs().get(0);
            Signal outSignal = outPort.getSignal();

            if (outSignal != null) {
                inExpr.setSignal(outSignal);
                vcBuilder.setCodeIrForSignal(outSignal, inExpr);
            }
        } else {
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
        }
    }
}
