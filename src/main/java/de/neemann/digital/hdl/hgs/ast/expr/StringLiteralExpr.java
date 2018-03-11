/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.expr;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.hdl.hgs.rt.StringValue;

/**
 *
 * @author ideras
 */
public class StringLiteralExpr extends Expr {
    private final String value;

    /**
     * Creates a new instance
     *
     * @param value the string value
     */
    public StringLiteralExpr(String value) {
        super(0);
        this.value = value;
    }

    /**
     * Return the string value
     *
     * @return the string value
     */
    public String getValue() {
        return value;
    }

    @Override
    public RtValue evaluate(HGSRuntimeContext ctx) throws HGSException {
        return new StringValue(value);
    }
}
