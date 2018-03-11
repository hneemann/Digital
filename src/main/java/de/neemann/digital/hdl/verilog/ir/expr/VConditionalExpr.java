/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VOperator;

/**
 * A node to represent a expression of the form: "cond? expr1 : expr2"
 * @author ideras
 */
public class VConditionalExpr extends VExpr {
    private final VExpr cond;
    private final VExpr expr1;
    private final VExpr expr2;

    /**
     * Initialize a new instance
     *
     * @param cond the condition
     * @param expr1 the first expression
     * @param expr2 the second expression
     */
    public VConditionalExpr(VExpr cond, VExpr expr1, VExpr expr2) {
        super();
        this.cond = cond;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    /**
     * Returns the condition
     *
     * @return the condition
     */
    public VExpr getCond() {
        return cond;
    }

    /**
     * Returns the first expression
     *
     * @return the first expression
     */
    public VExpr getExpr1() {
        return expr1;
    }

    /**
     * Returns the second expression
     *
     * @return the second expression
     */
    public VExpr getExpr2() {
        return expr2;
    }

    @Override
    public VOperator getOper() {
        return VOperator.CONDITIONAL;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        String condCode = cond.getSourceCode(vcBuilder);

        if (cond.getOper().getPrecedence() < getOper().getPrecedence()) {
            condCode = "(" + condCode + ")";
        }

        String code1 = expr1.getSourceCode(vcBuilder);
        String code2 = expr2.getSourceCode(vcBuilder);

        return (condCode + " ? " + code1 + " : " + code2);
    }
}
