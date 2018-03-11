/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir;

/**
 * Verilog Operator.
 *
 * This class represents a verilog operator in a expression.
 *
 * @author ideras
 */
public class VOperator {

    private final String symbol;
    private final int prec;
    private final boolean inverted;

    /**
     * Initialize a new operator
     *
     * @param symbol the operator symbol
     * @param prec the precedence
     * @param inverted true if the operator is inverted
     */
    public VOperator(String symbol, int prec, boolean inverted) {
        this.symbol = symbol;
        this.prec = prec;
        this.inverted = inverted;
    }

    /**
     * Initialize a new operator
     *
     * @param symbol the symbol
     * @param prec the precedence
     */
    public VOperator(String symbol, int prec) {
        this(symbol, prec, false);
    }

    /**
     * Returns the symbol
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Return the precedence
     *
     * @return the precedence
     */
    public int getPrecedence() {
        return prec;
    }

    /**
     * Checks if the operator is inverted.
     *
     * @return true if the operator is inverted, false otherwise.
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * slice operator
     */
    public static final VOperator PART_SELECT = new VOperator("[]", 17);
    /**
     * Concat operator
     */
    public static final VOperator CONCAT = new VOperator("{}", 13);
    /**
     * And operator
     */
    public static final VOperator AND = new VOperator("&", 5);
    /**
     * Or operator
     */
    public static final VOperator OR = new VOperator("|", 3);
    /**
     * Nand operator
     */
    public static final VOperator NAND = new VOperator("&", 5, true);
    /**
     * Nor operator
     */
    public static final VOperator NOR = new VOperator("|", 3, true);
    /**
     * Xor operator
     */
    public static final VOperator XOR = new VOperator("^", 4);
    /**
     * XNor operator
     */
    public static final VOperator XNOR = new VOperator("^", 4, true);

    /**
     * Reduction And operator
     */
    public static final VOperator RED_AND = new VOperator("&", 15);
    /**
     * Reduction Or operator
     */
    public static final VOperator RED_OR = new VOperator("|", 15);
    /**
     * Reduction Nand operator
     */
    public static final VOperator RED_NAND = new VOperator("~&", 15, true);
    /**
     * Reduction Nor operator
     */
    public static final VOperator RED_NOR = new VOperator("~|", 15, true);
    /**
     * Reduction Xor operator
     */
    public static final VOperator RED_XOR = new VOperator("^", 15);
    /**
     * Reduction XNor operator
     */
    public static final VOperator RED_XNOR = new VOperator("~^", 15, true);

    /**
     * Not operator
     */
    public static final VOperator NOT = new VOperator("~", 15, true);
    /**
     * Add operator
     */
    public static final VOperator ADD = new VOperator("+", 10);
    /**
     * Subtraction operator
     */
    public static final VOperator SUB = new VOperator("-", 10);
    /**
     * Multiplication operator
     */
    public static final VOperator MUL = new VOperator("*", 11);
    /**
     * Shift left operator
     */
    public static final VOperator SHL = new VOperator("<<", 9);
    /**
     * Shift right operator
     */
    public static final VOperator SHR = new VOperator(">>", 9);
    /**
     * Less than operator
     */
    public static final VOperator LT = new VOperator("<", 8);
    /**
     * Greater than operator
     */
    public static final VOperator GT = new VOperator(">", 8);
    /**
     * Equal operator
     */
    public static final VOperator EQ = new VOperator("==", 7);
    /**
     * Conditional operator
     */
    public static final VOperator CONDITIONAL = new VOperator("?:", 0);

    /**
     * No operator
     */
    public static final VOperator NONE = new VOperator("", 20);
}
