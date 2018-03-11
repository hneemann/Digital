/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.expr;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.ast.lvalue.LValue;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public class LValueExpr extends Expr {
    private final LValue lvalue;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param lvalue the left value
     */
    public LValueExpr(int line, LValue lvalue) {
        super(line);
        this.lvalue = lvalue;
    }

    /**
     * Returns the left value.
     *
     * @return the left value
     */
    public LValue getLvalue() {
        return lvalue;
    }

    @Override
    public RtValue evaluate(HGSRuntimeContext ctx) throws HGSException {
        RtReference vref = lvalue.getReference(ctx);

        return vref.getTarget();
    }
}
