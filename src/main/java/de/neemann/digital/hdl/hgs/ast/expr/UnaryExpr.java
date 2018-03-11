/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.expr;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public class UnaryExpr extends Expr {
    private final Expr expr;
    private final Operator oper;

    /**
     * Unary expression operators
     */
    public enum Operator {
        /**
         * Plus
         */
        ADD,
        /**
         * Minus
         */
        SUB,
        /**
         * Logical not
         */
        LNOT,
        /**
         * Bitwise not
         */
        NOT
    };

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param expr the expression
     * @param oper the operator to apply
     */
    public UnaryExpr(int line, Expr expr, Operator oper) {
        super(line);
        this.expr = expr;
        this.oper = oper;
    }

    /**
     * Return the expression
     *
     * @return the expression
     */
    public Expr getExpr() {
        return expr;
    }

    /**
     * Return the operator
     *
     * @return the operator
     */
    public Operator getOper() {
        return oper;
    }

    @Override
    public RtValue evaluate(HGSRuntimeContext ctx) throws HGSException {
        RtValue objValue = expr.evaluate(ctx);

        if (!(objValue instanceof IntValue)) {
            throw new HGSException(getLine(), "Only integer allowed in expressions.");
        }

        int value = ((IntValue) objValue).getValue();
        int result;

        switch (oper) {
            case ADD: result = value; break;
            case SUB: result = -value; break;
            case LNOT: result = (value != 0)? 0 : 1; break;
            case NOT: result = ~value; break;
            default:
                throw new HGSException(getLine(), "Invalid operator in unary expression: '" + oper.toString() + "'");
        }

        return new IntValue(result);
    }
}
