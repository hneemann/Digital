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
public class BinaryExpr extends Expr {
    private final Expr expr1;
    private final Expr expr2;
    private final Operator oper;

    /**
     * Binary operators
     */
    public enum Operator {
        /**
         * Addition
         */
        ADD,
        /**
         * Subtraction
         */
        SUB,
        /**
         * Multiplication
         */
        MULT,
        /**
         * Division
         */
        DIV,
        /**
         * Module
         */
        MOD,
        /**
         * Shift Left
         */
        SL,
        /**
         * Shift Right
         */
        SR,
        /**
         * Logical And
         */
        LAND,
        /**
         * Logical Or
         */
        LOR,
        /**
         * Bitwise And
         */
        AND,
        /**
         * Bitwise Or
         */
        OR,
        /**
         * Bitwise
         */
        XOR,
        /**
         * Less than
         */
        LT,
        /**
         * Greater than
         */
        GT,
        /**
         * Less or equal
         */
        LE,
        /**
         * Greater or equal
         */
        GE,
        /**
         * Equal
         */
        EQ,
        /**
         * Not equal
         */
        NE
    };

    /**
     * Creates a new binary expression node
     *
     * @param line the source line
     * @param expr1 the first expression operand
     * @param expr2 the second expression operand
     * @param oper the applied operator
     */
    public BinaryExpr(int line, Expr expr1, Expr expr2, Operator oper) {
        super(line);
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.oper = oper;
    }

    /**
     * Return the first operand
     *
     * @return the first operand
     */
    public Expr getExpr1() {
        return expr1;
    }

    /**
     * Return the second operand
     *
     * @return the second operand
     */
    public Expr getExpr2() {
        return expr2;
    }

    /**
     * Returns the operator
     *
     * @return the operator
     */
    public Operator getOper() {
        return oper;
    }

    @Override
    public RtValue evaluate(HGSRuntimeContext ctx) throws HGSException {
        RtValue rtvalue1 = expr1.evaluate(ctx);
        RtValue rtvalue2 = expr2.evaluate(ctx);

        if (!(rtvalue1 instanceof IntValue)) {
            throw new HGSException(getLine(), "Invalid variable in binary expression. Only integers allowed.");
        }
        if (!(rtvalue2 instanceof IntValue)) {
            throw new HGSException(getLine(), "Invalid variable in binary expression. Only integers allowed.");
        }

        int value1 = ((IntValue) rtvalue1).getValue();
        int value2 = ((IntValue) rtvalue2).getValue();
        int result;

        switch (oper) {
            case ADD: result = value1 + value2; break;
            case SUB: result = value1 - value2; break;
            case MULT: result = value1 * value2; break;
            case DIV: result = value1 / value2; break;
            case MOD: result = value1 % value2; break;
            case SL: result = value1 << value2; break;
            case SR: result = value1 >> value2; break;
            case LAND: result = (value1!=0 && value2!=0)? 1 : 0; break;
            case LOR: result = (value1!=0 || value2!=0)? 1 : 0; break;
            case AND: result = value1 & value2; break;
            case OR: result = value1 | value2; break;
            case XOR: result = value1 ^ value2; break;
            case LT: result = (value1 < value2)? 1 : 0; break;
            case GT: result = (value1 > value2)? 1 : 0; break;
            case LE: result = (value1 <= value2)? 1 : 0; break;
            case GE: result = (value1 >=value2)? 1 : 0; break;
            case EQ: result = (value1 == value2)? 1 : 0; break;
            case NE: result = (value1 != value2)? 1 : 0; break;
            default:
                throw new HGSException(getLine(), "Invalid operator in binary expression: '" + oper.toString() + "'");
        }

        return new IntValue(result);
    }
}
