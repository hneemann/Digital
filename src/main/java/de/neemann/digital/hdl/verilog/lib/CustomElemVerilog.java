/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.hdl.verilog.ir.stmt.VInstanceMapping;
import de.neemann.digital.hdl.verilog.ir.stmt.VInstanceBlock;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class CustomElemVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public CustomElemVerilog() {
        super(null);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        ArrayList<VInstanceMapping> signalMappings = new ArrayList<>();

        VInstanceBlock stmt = new VInstanceBlock(node.getHDLName(), node.getHDLName(), signalMappings);

        for (Port p : node.getPorts().getInputs()) {
            VIRNode irnode = vcBuilder.getSignalCodeIr(p.getSignal());
            VExpr inExpr = irnode.resolveToExpr(vcBuilder);

            signalMappings.add(new VInstanceMapping(p.getName(), inExpr));
        }

        for (Port p : node.getPorts().getOutputs()) {
            if (p.getSignal() == null) {
                continue;
            }
            Signal s = p.getSignal();
            String signalName = s.getName();
            VExpr expr = new VIdExpr(signalName);

            signalMappings.add(new VInstanceMapping(p.getName(), expr));
            vcBuilder.registerAndAddSignalDecl(s, VSignalDecl.Type.WIRE);
            expr.setSignal(s);
            vcBuilder.setCodeIrForSignal(signalName, expr);
        }

        vcBuilder.registerStatement(stmt, null);
    }

}
