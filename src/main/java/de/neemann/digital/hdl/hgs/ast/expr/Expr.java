/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.expr;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.ast.ASTNode;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public abstract class Expr extends ASTNode {
    /**
     * Expression AST node base constructor
     *
     * @param line the source line
     */
    public Expr(int line) {
        super(line);
    }

    /**
     * Evaluates the expression
     *
     * @param ctx the runtime context instance
     * @return the expression value
     * @throws HGSException HDLGenException
     */
    public abstract RtValue evaluate(HGSRuntimeContext ctx) throws HGSException;
}
