/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;

/**
 *
 * @author ideras
 */
public class VIfStatementItem {
    private final VExpr condition;
    private final VStatement statement;

    /**
     * Creates a new instance
     *
     * @param condition the condition
     * @param statement the statement
     */
    public VIfStatementItem(VExpr condition, VStatement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    /**
     * Returns the condition
     *
     * @return the condition
     */
    public VExpr getCondition() {
        return condition;
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
