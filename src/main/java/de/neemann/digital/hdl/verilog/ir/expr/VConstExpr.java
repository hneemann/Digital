/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;

/**
 * Constant expression.
 *
 * A expression with a constant value.
 *
 * @author ideras
 */
public class VConstExpr extends VExpr {
    private final long value;
    private final int bits;
    private final Type type;

    /**
     * Constant type
     */
    public enum Type {
        /**
         * High Z value
         */
        Z,
        /**
         * Undefined value
         */
        X,
        /**
         * Defined value
         */
        Normal
    };

    /**
     * Initialize a new instance
     *
     * @param bits the number of bits
     * @param value the constant value
     * @param type the constant type
     */
    private VConstExpr(int bits, long value, Type type) {
        super();
        this.bits = bits;
        this.value = value;
        this.type = type;
    }

    /**
     * Initialize a new instance
     *
     * @param bits the number of bits
     * @param value the constant value
     */
    public VConstExpr(int bits, long value) {
        this(bits, value, Type.Normal);
    }

    /**
     * Initialize a new instance
     *
     * @param bits the number of bits
     * @param type the constant type
     */
    public VConstExpr(int bits, Type type) {
        this(bits, 0, type);
    }

    /**
     * Return the constant value
     *
     * @return the constant value
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns the number of bits
     *
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * Returns the type of the constant value.
     *
     * @return the constant type
     */
    public Type getType() {
        return type;
    }

    @Override
    public VExpr resolveToIdExpr(VerilogCodeBuilder vcBuilder) {
        return this;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        String result = bits + "'h";

        switch (type) {
            case Z: result += "Z"; break;
            case X: result += "X"; break;
            default:
                result += Long.toHexString(truncValue());
        }

        return result;
    }

    private long truncValue() {
        long mask = (1L << bits) - 1;

        return (value & mask);
    }
}
