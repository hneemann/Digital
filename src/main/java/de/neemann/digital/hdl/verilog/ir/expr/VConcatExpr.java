/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import java.util.ArrayList;

/**
 * Concatenation Expression.
 *
 * A expression that concatenates various expressions.
 *
 * @author ideras
 */
public class VConcatExpr extends VExpr {
    private ArrayList<VExpr> exprList;

    /**
     * Creates a new instance
     *
     * @param exprList the list of expressions
     */
    public VConcatExpr(ArrayList<VExpr> exprList) {
        super();
        this.exprList = exprList;
    }

    /**
     * Creates a new instance
     *
     * @param exprs the array of expressions
     */
    public VConcatExpr(VExpr... exprs) {
        super();
        exprList = new ArrayList<>();

        for (VExpr e : exprs) {
            exprList.add(e);
        }
    }

    /**
     * Return an expression at the specified index.
     *
     * @param index the index
     * @return the expression at the specified index.
     */
    public VIRNode getExprAt(int index) {
        return exprList.get(index);
    }

    @Override
    public VOperator getOper() {
        return VOperator.CONCAT;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        VExpr expr = exprList.get(0);

        String resultCode = "{" + expr.getSourceCode(vcBuilder);

        for (int i = 1; i < exprList.size(); i++) {
            expr = exprList.get(i);
            resultCode += ", " + expr.getSourceCode(vcBuilder);
        }
        resultCode += "}";

        return resultCode;
    }
}
