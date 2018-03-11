/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.ast.ASTNode;

/**
 *
 * @author ideras
 */
public abstract class Statement extends ASTNode {
    /**
     * The base statement constructor
     *
     * @param line the source line
     */
    public Statement(int line) {
        super(line);
    }

    /**
     * Execute the statement.
     *
     * @param ctx the runtime context
     * @throws HGSException HDLGenException
     */
    public abstract void execute(HGSRuntimeContext ctx) throws HGSException;
}
