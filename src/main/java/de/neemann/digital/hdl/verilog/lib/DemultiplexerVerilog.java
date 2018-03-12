/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Demultiplexer;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.expr.VBinaryExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VConditionalExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;
import java.util.ArrayList;

/**
 * Demultiplexer verilog element
 *
 * @author ideras
 */
public class DemultiplexerVerilog extends VerilogElement {

    /**
     * Initialize a new instance
     */
    public DemultiplexerVerilog() {
        super(Demultiplexer.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int dataBits = node.get(Keys.BITS);
        int selBits = node.get(Keys.SELECTOR_BITS);

        Signal selSignal = node.getPorts().getInputs().get(0).getSignal();
        Signal inSignal = node.getPorts().getInputs().get(1).getSignal();
        VIRNode selNode = vcBuilder.getSignalCodeIr(selSignal);
        VIRNode inNode = vcBuilder.getSignalCodeIr(inSignal);
        VExpr selExpr = selNode.resolveToExpr(vcBuilder);
        VExpr inExpr = inNode.resolveToExpr(vcBuilder);

        ArrayList<Port> outputPorts = node.getPorts().getOutputs();
        for (int i = 0; i < outputPorts.size(); i++) {
            Signal outSignal = outputPorts.get(i).getSignal();
            VStatementPlace outPlace = new VStatementPlace(outSignal);
            VStatement stmt = new VAssignStatement(outPlace,
                                                   new VConditionalExpr(
                                                           new VBinaryExpr(selExpr, new VConstExpr(selBits, i), VOperator.EQ),
                                                           inExpr,
                                                           new VConstExpr(dataBits, 0))
                                                   );

            vcBuilder.registerSignalDecl(outSignal, VSignalDecl.Type.WIRE);
            vcBuilder.setCodeIrForSignal(outSignal, stmt);
        }
    }

}
