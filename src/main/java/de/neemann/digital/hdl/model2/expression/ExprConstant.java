/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;

import de.neemann.digital.core.Bits;
import de.neemann.digital.hdl.model2.HDLNet;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.model2.HDLNodeAssignment;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Represents a constant
 */
public class ExprConstant implements Expression {

    private long value;
    private int bits;

    /**
     * Creates a new constant
     *
     * @param constant the constant
     */
    public ExprConstant(ExprConstant constant) {
        this(constant.getValue(), constant.getBits());
    }

    /**
     * Creates a new constant
     *
     * @param value the value
     * @param bits  the number of bits
     */
    public ExprConstant(long value, int bits) {
        this.bits = bits;
        this.value = value & Bits.mask(bits);
    }

    /**
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print(value).print(":").print(bits);
    }

    @Override
    public void replace(HDLNet net, Expression expression) {
    }

    /**
     * Returns true if this constant is equal to the given constant.
     *
     * @param other the given constant
     * @return true if both constants are equal
     */
    public boolean isEqualTo(ExprConstant other) {
        return bits == other.bits && value == other.value;
    }

    /**
     * Returns the constant if th given node represents a constant
     *
     * @param node the node
     * @return the constant of null if node is not a constant
     */
    public static ExprConstant isConstant(HDLNode node) {
        if (node instanceof HDLNodeAssignment) {
            HDLNodeAssignment expr = (HDLNodeAssignment) node;
            if (expr.getExpression() instanceof ExprConstant) {
                return (ExprConstant) expr.getExpression();
            }
        }

        return null;
    }

}
