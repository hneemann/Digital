/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;

/**
 * Represents a case statement item.
 *
 * @author ideras
 */
public class VCaseStatementItem {
   private final VExpr expr;
   private final VStatement statement;

    /**
    * Creates a new instance
    *
    * @param expr the expression
    * @param statement the statement
    */
    public VCaseStatementItem(VExpr expr, VStatement statement) {
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
}
