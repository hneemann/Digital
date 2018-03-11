/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.expr;

import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public class ConstantExpr extends Expr {
    private final int value;

    /**
     * Creates a new instance
     *
     * @param value the value
     */
    public ConstantExpr(int value) {
        super(0);
        this.value = value;
    }

    /**
     * Returns the value
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    @Override
    public RtValue evaluate(HGSRuntimeContext ctx) {
        return new IntValue(value);
    }
}
