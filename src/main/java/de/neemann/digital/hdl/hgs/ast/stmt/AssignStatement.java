/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.ast.expr.Expr;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.ast.lvalue.LValue;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 * Represents the assign statement node
 *
 * @author ideras
 */
public class AssignStatement extends Statement {
    private final LValue lvalue;
    private final Expr expr;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param lvalue the left value of the assignment
     * @param expr the expression to assign
     */
    public AssignStatement(int line, LValue lvalue, Expr expr) {
        super(line);
        this.lvalue = lvalue;
        this.expr = expr;
    }

    /**
     * Returns the left value
     *
     * @return the left value
     */
    public LValue getLvalue() {
        return lvalue;
    }

    /**
     * Returns the expression
     *
     * @return the expression
     */
    public Expr getExpr() {
        return expr;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        RtValue value = expr.evaluate(ctx);
        RtReference vref;

        try {
            vref = lvalue.getReference(ctx);
        } catch (HGSException ex) {
            vref = lvalue.register(ctx, value.getType());
        }

        vref.set(value);
    }
}
