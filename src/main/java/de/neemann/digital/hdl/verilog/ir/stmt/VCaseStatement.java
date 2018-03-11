/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a verilog case statement.
 *
 * @author ideras
 */
public class VCaseStatement extends VStatement {
    private final VExpr selectExpr;
    private final ArrayList<VCaseStatementItem> itemList;

    /**
     * Creates a new instance.
     *
     * @param selectExpr the expr of the case statement.
     * @param itemList   the list of items in the case statement.
     */
    public VCaseStatement(VExpr selectExpr, ArrayList<VCaseStatementItem> itemList) {
        this(selectExpr, itemList, null);
    }

    /**
     * Creates a new instance.
     *
     * @param selectExpr the expr of the case statement.
     * @param itemList   the list of items in the case statement.
     * @param place      the name of the signal this statement computes.
     */
    public VCaseStatement(VExpr selectExpr, ArrayList<VCaseStatementItem> itemList, VStatementPlace place) {
        super(place);
        this.selectExpr = selectExpr;
        this.itemList = itemList;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        String selExprCode = selectExpr.getSourceCode(vcBuilder);

        out.print("case (").print(selExprCode).println(")");

        out.inc();
        for (VCaseStatementItem caseItem : itemList) {
            VExpr caseExpr = caseItem.getExpr();
            VStatement caseSt = caseItem.getStatement();

            out.print(caseExpr.getSourceCode(vcBuilder)).println(":");
            out.inc();
            caseSt.writeSourceCode(vcBuilder, out);
            out.dec();
            out.println();
        }
        out.dec();
        out.print("endcase");
    }
}
