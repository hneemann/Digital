/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.vhdl.Separator;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a verilog if statement
 *
 * @author ideras
 */
public class VIfStatement extends VStatement {
    private ArrayList<VIfStatementItem> itemList;
    private final VStatement elseStatement;

    /**
     * Creates a new instance
     *
     * @param itemList      the list of items (condition, statement)
     */
    public VIfStatement(ArrayList<VIfStatementItem> itemList) {
        this(itemList, null, null);
    }

    /**
     * Creates a new instance
     *
     * @param itemList      the list of items (condition, statement)
     * @param elseStatement the else statement
     */
    public VIfStatement(ArrayList<VIfStatementItem> itemList, VStatement elseStatement) {
        this(itemList, elseStatement, null);
    }

    /**
     * Creates a new instance
     *
     * @param cond          the condition
     * @param trueStatement the statement to execute if the condition is true.
     * @param elseStatement the else statement
     */
    public VIfStatement(VExpr cond, VStatement trueStatement, VStatement elseStatement) {
        super(null);
        itemList = new ArrayList<>();
        itemList.add(new VIfStatementItem(cond, trueStatement));
        this.elseStatement = elseStatement;
    }

        /**
     * Creates a new instance
     *
     * @param cond          the condition
     * @param trueStatement the statement to execute if the condition is true.
     */
    public VIfStatement(VExpr cond, VStatement trueStatement) {
        this(cond, trueStatement, (VStatement) null);
    }

    /**
     * Creates a new instance
     *
     * @param itemList      the list of items (condition, statement)
     * @param elseStatement the else statement
     * @param place         the name of the signal this statement computes.
     */
    public VIfStatement(ArrayList<VIfStatementItem> itemList, VStatement elseStatement, VStatementPlace place) {
        super(place);
        this.itemList = itemList;
        this.elseStatement = elseStatement;
    }

    /**
     * Returns the list of items
     *
     * @return the list of items
     */
    public ArrayList<VIfStatementItem> getItemList() {
        return itemList;
    }

    /**
     * Returns the "else" statement
     *
     * @return the "else" statement
     */
    public VIRNode getElseStatement() {
        return elseStatement;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        boolean first = true;

        Separator eol = new Separator("\n");
        for (VIfStatementItem item : itemList) {
            VExpr cond = (VExpr) item.getCondition();
            VStatement st = (VStatement) item.getStatement();

            eol.check(out);
            if (first) {
                out.print("if (").print(cond.getSourceCode(vcBuilder)).println(")");
                first = false;
            } else {
                out.print("else if (").print(cond.getSourceCode(vcBuilder)).println(")");
            }
            out.inc();
            st.writeSourceCode(vcBuilder, out);
            out.dec();
        }

        if (elseStatement != null) {
            out.println().println("else");
            out.inc();
            elseStatement.writeSourceCode(vcBuilder, out);
            out.dec();
        }
    }
}
