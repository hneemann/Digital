/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;


/**
 * Intermediate representation node.
 *
 * @author ideras
 */
public abstract class VIRNode {

    /**
     * Base constructor
     */
    public VIRNode() {
    }

    /**
     * Returns true is this node represents a expression.
     *
     * @return true is this node is a expression, false otherwise.
     */
    public boolean isExpr() {
        return false;
    }

    /**
     * Return true is this node represents a statement.
     *
     * @return true is this node is a statement, false otherwise.
     */
    public boolean isStatement() {
        return false;
    }

    /**
     * Returns an expression representing the node.
     * If the node is a statement it's registered in the builder.
     *
     * @param vcBuilder the verilog builder instance.
     * @return the generated expression.
     */
    public abstract VExpr resolveToExpr(VerilogCodeBuilder vcBuilder);
}
