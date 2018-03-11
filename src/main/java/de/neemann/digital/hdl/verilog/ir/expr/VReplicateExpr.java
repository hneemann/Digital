/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;

/**
 *
 * @author ideras
 */
public class VReplicateExpr extends VExpr {
    private final int count;
    private final VExpr expr;

    /**
     * Initialize a new replicate expr
     *
     * @param count the count
     * @param expr the expression
     */
    public VReplicateExpr(int count, VExpr expr) {
        super();
        this.count = count;
        this.expr = expr;
    }

    /**
     * Returns the count
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the expression
     *
     * @return the expression
     */
    public VExpr getExpr() {
        return expr;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        VExpr replExpr = expr.resolveToExpr(vcBuilder);

        return "{" + count + "{" + replExpr.getSourceCode(vcBuilder) + "}}";
    }
}
