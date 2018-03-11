/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class CompoundStatement extends Statement {
    private final ArrayList<Statement> statementList;

    /**
     * Creates a new instance
     *
     * @param statementList the list of statements
     */
    public CompoundStatement(ArrayList<Statement> statementList) {
        super(0);
        this.statementList = statementList;
    }

    /**
     * Returns the statement list.
     *
     * @return the statement list
     */
    public ArrayList<Statement> getStatementList() {
        return statementList;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        for (Statement stmt : statementList) {
            stmt.execute(ctx);
        }
    }

}
