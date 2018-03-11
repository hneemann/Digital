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
public class VInstanceMapping {
    private final String signalName;
    private final VExpr argExpr;

    /**
     * Creates a new instance
     *
     * @param signalName the signal name
     * @param argExpr the expression argument
     */
    public VInstanceMapping(String signalName, VExpr argExpr) {
        this.signalName = signalName;
        this.argExpr = argExpr;
    }

    /**
     * Returns the signal name
     *
     * @return the signal name
     */
    public String getSignalName() {
        return signalName;
    }

    /**
     * Returns the argument expression
     *
     * @return the argument expression
     */
    public VExpr getArgExpr() {
        return argExpr;
    }
}
