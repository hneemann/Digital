/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Multiplexer;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VCaseStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VCaseStatementItem;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VConstExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class MultiplexerVerilog extends VerilogElement {

    /**
     * Creates a new instance
     */
    public MultiplexerVerilog() {
        super(Multiplexer.DESCRIPTION);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        int selBits = node.get(Keys.SELECTOR_BITS);
        int dataBits = node.get(Keys.BITS);
        int size = 1 << selBits;
        Signal selSignal = node.getPorts().getInputs().get(0).getSignal();
        Signal outSignal = node.getPorts().getOutputs().get(0).getSignal();
        VIRNode selExprNode = vcBuilder.getSignalCodeIr(selSignal);
        VExpr selExpr = selExprNode.resolveToExpr(vcBuilder);

        ArrayList<VCaseStatementItem> caseItemList = new ArrayList<>();
        VStatementPlace outPlace = new VStatementPlace(outSignal);
        VStatement casest = new VCaseStatement(selExpr, caseItemList, outPlace);

        vcBuilder.registerSignalDecl(outSignal, VSignalDecl.Type.REG);
        for (int i = 1; i <= size; i++) {
            Signal inSignal = node.getPorts().getInputs().get(i).getSignal();
            VIRNode caseExprNode = new VConstExpr(selBits, i-1);
            VIRNode inIRNode = vcBuilder.getSignalCodeIr(inSignal);
            VExpr caseExpr = caseExprNode.resolveToExpr(vcBuilder);
            VStatement stmt;

            if (inIRNode.isExpr()) {
                stmt = new VAssignStatement(outPlace, (VExpr) inIRNode);
            } else if (inIRNode.isStatement()) {
                stmt = (VStatement) inIRNode;

                if (inSignal.getPorts().size() != 2) {
                    vcBuilder.registerStatement(stmt, inSignal);
                    stmt = new VAssignStatement(outPlace, new VIdExpr(inSignal.getName()));
                }
            } else {
                throw new RuntimeException("BUG in the machine: Invalid irnode. " + inIRNode.getClass().toString());
            }

            stmt.setParent(casest);
            VCaseStatementItem caseItem = new VCaseStatementItem(caseExpr, stmt);

            caseItemList.add(caseItem);
        }

        vcBuilder.setCodeIrForSignal(outSignal.getName(), casest);
    }

}
