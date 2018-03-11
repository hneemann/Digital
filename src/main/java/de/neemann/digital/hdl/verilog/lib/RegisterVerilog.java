/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.memory.Register;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAlwaysBlock;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VIfStatement;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;

/**
 *
 * @author ideras
 */
public class RegisterVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public RegisterVerilog() {
        super(Register.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        Signal dInSignal = node.getPorts().getInputs().get(0).getSignal();
        Signal clkInSignal = node.getPorts().getInputs().get(1).getSignal();
        Signal enInSignal = node.getPorts().getInputs().get(2).getSignal();
        Signal qSignal = node.getPorts().getOutputs().get(0).getSignal();
        VStatementPlace qPlace = new VStatementPlace(qSignal);

        VIRNode clkIrNode = vcBuilder.getSignalCodeIr(clkInSignal);
        VIRNode dIrNode = vcBuilder.getSignalCodeIr(dInSignal);
        VIRNode enIrNode = vcBuilder.getSignalCodeIr(enInSignal);
        VExpr clkExpr = clkIrNode.resolveToExpr(vcBuilder);
        VExpr dExpr = dIrNode.resolveToExpr(vcBuilder);
        VExpr enExpr = enIrNode.resolveToExpr(vcBuilder);

        VAlwaysBlock alwaysBlock = new VAlwaysBlock(
                                        qPlace,
                                        clkExpr,
                                        VAlwaysBlock.Event.POSEDGE,
                                        new VIfStatement(enExpr, new VAssignStatement(
                                                                        qPlace,
                                                                        dExpr,
                                                                        VAssignStatement.Type.NON_BLOCKING
                                                                 )
                                        )
                                    );

        vcBuilder.registerAndAddSignalDecl(qSignal, VSignalDecl.Type.REG);
        vcBuilder.setCodeIrForSignal(qSignal, new VIdExpr(qSignal.getName()));
        vcBuilder.registerStatement(alwaysBlock, null);
        vcBuilder.addInitialStatement(new VAssignStatement(qPlace, new VConstExpr(qSignal.getBits(), 0)));
    }

}
