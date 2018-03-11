/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;


/**
 * Identifier expression.
 *
 * A expression with a simple identifier.
 *
 * @author ideras
 */
public class VIdExpr extends VExpr {
    private final String id;

    /**
     * Creates a new instance
     *
     * @param id the identifier
     */
    public VIdExpr(String id) {
        super();
        this.id = id;
    }

    /**
     * Returns the identifier
     *
     * @return the identifier
     */
    public String getIdent() {
        return id;
    }

    @Override
    public VExpr resolveToIdExpr(VerilogCodeBuilder vcBuilder) {
        return this;
    }

    @Override
    public String getSourceCode(VerilogCodeBuilder vcBuilder) {
        return id;
    }
}
