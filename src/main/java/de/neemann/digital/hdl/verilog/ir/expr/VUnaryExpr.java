/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;

/**
 * Unary Expression.
 *
 * A expression with one operator and one operand.
 *
 * @author ideras
 */
public class VUnaryExpr extends VExpr {
    private final VExpr expr;
    private final VOperator oper;

    /**
     * Initialize a new unary expression instance
     *
     * @param expr the expression
     * @param oper the operator
     */
    public VUnaryExpr(VExpr expr, VOperator oper) {
        super();
        this.expr = expr;
        this.oper = oper;
    }

    /**
     * Return the expression
     * @return the expression
     */
    public VIRNode getExpr() {
        return expr;
    }

    @Override
    public VOperator getOper() {
        return oper;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        String sourceCode = expr.getSourceCode(vcBuilder);

        if (expr.getOper().getPrecedence() < oper.getPrecedence()) {
            sourceCode = "(" + sourceCode + ")";
        }

        return oper.getSymbol() + sourceCode;
    }
}
