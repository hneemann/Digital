/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.ast.expr.Expr;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public class PrintStatement extends Statement {
    private final Expr expr;

    /**
     * Create a new instances.
     *
     * @param line the source line.
     * @param expr the expression to print.
     */
    public PrintStatement(int line, Expr expr) {
        super(line);
        this.expr = expr;
    }

    /**
     * Returns the expression.
     *
     * @return the expression.
     */
    public Expr getExpr() {
        return expr;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        RtValue objValue = expr.evaluate(ctx);

        ctx.print(objValue);
    }

}
