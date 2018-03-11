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
public class VSignedExpr extends VExpr {
    private final VExpr expr;

    /**
     * Initialize a new signed expression
     *
     * @param expr the expression
     */
    public VSignedExpr(VExpr expr) {
        super();
        this.expr = expr;
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
        String sourceCode = expr.getSourceCode(vcBuilder);

        return "$signed(" + sourceCode + ")";
    }

}
