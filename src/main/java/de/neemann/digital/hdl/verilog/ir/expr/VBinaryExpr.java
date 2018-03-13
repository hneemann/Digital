/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VOperator;

/**
 *
 * Represents a generated expression with two operands.
 *
 * @author ideras
 */
public class VBinaryExpr extends VExpr {
    private final VExpr leftExpr;
    private final VExpr rightExpr;
    private final VOperator oper;

    /**
     * Creates a new instance
     *
     * @param leftExpr the first expression
     * @param rightExpr the second expression
     * @param oper the operator
     */
    public VBinaryExpr(VExpr leftExpr, VExpr rightExpr, VOperator oper) {
        super();
        this.leftExpr = leftExpr;
        this.rightExpr = rightExpr;
        this.oper = oper;
    }

    /**
     * Returns the first expression node
     *
     * @return the first expression node
     */
    public VExpr getLeftExpr() {
        return leftExpr;
    }

    /**
     * Returns the second expression node
     *
     * @return the second expression node
     */
    public VExpr getRightExpr() {
        return rightExpr;
    }

    @Override
    public VOperator getOper() {
        return oper;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        String leftCode = leftExpr.getSourceCode(vcBuilder);
        String rightCode = rightExpr.getSourceCode(vcBuilder);

        if (leftExpr.getOper().getPrecedence() < oper.getPrecedence()) {
            leftCode = "(" + leftCode + ")";
        }
        if (rightExpr.getOper().getPrecedence() < oper.getPrecedence()) {
            rightCode = "(" + rightCode + ")";
        }

        String resultCode = leftCode + " " + oper.getSymbol() + " " + rightCode;

        if (oper.isInverted()) {
            if (oper.getPrecedence() < VOperator.NOT.getPrecedence()) {
                resultCode = "~(" + resultCode + ")";
            } else {
                resultCode = "~" + resultCode;
            }
        }

        return resultCode;
    }
}
