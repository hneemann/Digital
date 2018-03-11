/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.PriorityEncoder;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.expr.VConcatExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VIfStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VIfStatementItem;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class PriorityEncoderVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public PriorityEncoderVerilog() {
        super(PriorityEncoder.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int selBits = node.get(Keys.SELECTOR_BITS);
        ArrayList<Port> inputs = node.getPorts().getInputs();
        Signal numSignal = node.getPorts().getOutputs().get(0).getSignal();
        Signal anySignal = node.getPorts().getOutputs().get(1).getSignal();

        if (numSignal == null && anySignal == null) {
            return;
        }

        VStatementPlace outPlace = null;
        VExpr elseExpr = null;

        if (numSignal != null && anySignal != null) {
            outPlace = new VStatementPlace(numSignal, anySignal);
            elseExpr = new VConcatExpr(new VConstExpr(selBits, 0), new VConstExpr(1, 0));
        } else {
            if (numSignal != null) {
                outPlace = new VStatementPlace(numSignal);
                elseExpr = new VConstExpr(selBits, 0);
            }
            if (anySignal != null) {
                outPlace = new VStatementPlace(anySignal);
                elseExpr = new VConstExpr(1, 0);
            }
        }

        VStatement elseSt = new VAssignStatement(outPlace, elseExpr);
        ArrayList<VIfStatementItem> ifItemList = new ArrayList<>();
        VStatement ifStmt = new VIfStatement(ifItemList, elseSt, outPlace);

        for (int i = 0; i < inputs.size(); i++) {
            Signal inS = inputs.get(i).getSignal();
            VIRNode inCodeIr = vcBuilder.getSignalCodeIr(inS);
            VExpr inExpr = inCodeIr.resolveToExpr(vcBuilder);

            VExpr rightExpr = null;

            if (numSignal != null && anySignal != null) {
                rightExpr = new VConcatExpr(new VConstExpr(selBits, i), new VConstExpr(1, 1));
            } else {
                if (numSignal != null) {
                    rightExpr = new VConstExpr(selBits, i);
                }
                if (anySignal != null) {
                    rightExpr = new VConstExpr(1, 1);
                }
            }
            VStatement stmt = new VAssignStatement(outPlace, rightExpr);

            stmt.setParent(ifStmt);
            VIfStatementItem ifItem = new VIfStatementItem(inExpr, stmt);

            ifItemList.add(ifItem);
        }

        vcBuilder.registerStatement(ifStmt, null);

        if (numSignal != null) {
            vcBuilder.registerAndAddSignalDecl(numSignal, VSignalDecl.Type.REG);
            vcBuilder.setCodeIrForSignal(numSignal, new VIdExpr(numSignal.getName()));
        }

        if (anySignal != null) {
            vcBuilder.registerAndAddSignalDecl(anySignal, VSignalDecl.Type.REG);
            vcBuilder.setCodeIrForSignal(anySignal, new VIdExpr(anySignal.getName()));
        }
    }
}
