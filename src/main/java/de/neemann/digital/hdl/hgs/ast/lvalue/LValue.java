/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.lvalue;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.ast.ASTNode;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public abstract class LValue extends ASTNode {

    /**
     * Left value node base constructor
     *
     * @param line the source line
     */
    public LValue(int line) {
        super(line);
    }

    /**
     * Returns the reference of this lvalue.
     *
     * @param ctx the runtime context.
     * @return  the reference or null on error.
     * @throws HGSException HDLGenException
     */
    public abstract RtReference getReference(HGSRuntimeContext ctx) throws HGSException;

    /**
     * Register this lvalue in the specified context.
     *
     * @param ctx the runtime context.
     * @param type the type
     * @return the registered reference.
     * @throws HGSException HDLGenException
     */
    public abstract RtReference register(HGSRuntimeContext ctx, RtValue.Type type) throws HGSException;
}
