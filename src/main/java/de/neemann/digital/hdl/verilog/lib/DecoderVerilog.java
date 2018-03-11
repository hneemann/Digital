/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Decoder;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.expr.VBinaryExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VConditionalExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class DecoderVerilog extends VerilogElement {

    /**
     * Initialize a new instance
     */
    public DecoderVerilog() {
        super(Decoder.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int selBits = node.get(Keys.SELECTOR_BITS);

        String inSignalName = node.getPorts().getInputs().get(0).getSignal().getName();
        VIRNode inNode = vcBuilder.getSignalCodeIr(inSignalName);
        VExpr inExpr = inNode.resolveToExpr(vcBuilder);
        ArrayList<Port> outputPorts = node.getPorts().getOutputs();

        for (int i = 0; i < outputPorts.size(); i++) {
            Signal s = outputPorts.get(i).getSignal();
            VStatementPlace place = new VStatementPlace(s);

            vcBuilder.registerSignalDecl(s, VSignalDecl.Type.WIRE);

            VStatement stmt = new VAssignStatement(place,
                                    new VConditionalExpr(
                                        new VBinaryExpr(inExpr, new VConstExpr(selBits, i), VOperator.EQ),
                                            new VConstExpr(1, 1),
                                            new VConstExpr(1, 0)));

            vcBuilder.setCodeIrForSignal(outputPorts.get(i).getSignal(), stmt);
        }
    }

}
