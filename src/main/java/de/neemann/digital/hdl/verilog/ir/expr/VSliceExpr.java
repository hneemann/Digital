/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VOperator;

/**
 * Slice expression.
 *
 * A expression that selects part of the bits of another expression.
 *
 * @author ideras
 */
public class VSliceExpr extends VExpr {
    private VExpr expr;
    private VExpr startIndexExpr;
    private VExpr endIndexExpr;

    /**
     * Initialize a new slice expression
     *
     * @param expr the expression
     * @param startIndexExpr the start index expression
     * @param endIndexExpr the end index expression
     */
    public VSliceExpr(VExpr expr, VExpr startIndexExpr, VExpr endIndexExpr) {
        super();
        this.expr = expr;
        this.startIndexExpr = startIndexExpr;
        this.endIndexExpr = endIndexExpr;
    }

    /**
     * Initialize a new slice expression
     *
     * @param expr the expression
     * @param indexExpr the index expression
     */
    public VSliceExpr(VExpr expr, VExpr indexExpr) {
        this(expr, indexExpr, null);
    }

    /**
     * Initialize a new slice expression
     *
     * @param expr the expression
     * @param startIndex the start index
     * @param endIndex the end index
     */
    public VSliceExpr(VExpr expr, int startIndex, int endIndex) {
        this(expr, new VConstExpr(32, startIndex), new VConstExpr(32, endIndex));
    }

    /**
     * Initialize a new slice expression
     *
     * @param expr the expression
     * @param index the index
     */
    public VSliceExpr(VExpr expr, int index) {
        this(expr, new VConstExpr(32, index));
    }


    /**
     * Returns the expression
     *
     * @return the expression
     */
    public VExpr getExpr() {
        return expr;
    }

    /**
     * Returns the start index expression
     *
     * @return the start index expression
     */
    public VExpr getStartIndexExpr() {
        return startIndexExpr;
    }

    /**
     * Returns the end index
     *
     * @return the end index expression
     */
    public VExpr getEndIndexExpr() {
        return endIndexExpr;
    }

    @Override
    public VOperator getOper() {
        return VOperator.PART_SELECT;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        String exprCode = expr.getSourceCode(vcBuilder);

        if (expr.getOper().getPrecedence() < getOper().getPrecedence()) {
            exprCode = "(" + exprCode + ")";
        }
        String startIndexCode;
        String endIndexCode = "";

        if (startIndexExpr instanceof VConstExpr) {
            VConstExpr constExpr = (VConstExpr) startIndexExpr;
            startIndexCode = Integer.toString((int) constExpr.getValue());
        } else {
            startIndexCode = startIndexExpr.getSourceCode(vcBuilder);
        }

        if (endIndexExpr != null) {
            if (endIndexExpr instanceof VConstExpr) {
                VConstExpr constExpr = (VConstExpr) endIndexExpr;
                endIndexCode = Integer.toString((int) constExpr.getValue());
            } else {
                endIndexCode = endIndexExpr.getSourceCode(vcBuilder);
            }
        }

        if (endIndexExpr != null) {
            return exprCode + "[" + startIndexCode + ":" + endIndexCode + "]";
        } else {
            return exprCode + "[" + startIndexCode + "]";
        }
    }
}
