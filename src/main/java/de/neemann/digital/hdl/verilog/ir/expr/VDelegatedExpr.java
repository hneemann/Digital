/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;

/**
 *
 * @author ideras
 */
public class VDelegatedExpr extends VExpr {
    private final VExpr expr;
    private final VStatement statement;

    /**
     * Initialize a new instance
     *
     * @param expr the expression node
     * @param statement the statement node
     */
    public VDelegatedExpr(VExpr expr, VStatement statement) {
        this.expr = expr;
        this.statement = statement;
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
     * Returns the statement
     *
     * @return the statement
     */
    public VStatement getStatement() {
        return statement;
    }

    @Override
    public VExpr resolveToIdExpr(VerilogCodeBuilder vcBuilder) {
        return expr.resolveToIdExpr(vcBuilder);
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        return expr.getSourceCode(vcBuilder);
    }

}
