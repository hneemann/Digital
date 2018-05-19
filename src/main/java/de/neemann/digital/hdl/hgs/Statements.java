/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.util.ArrayList;

/**
 * A list of statements.
 */
class Statements implements Statement {
    private final ArrayList<Statement> statements;

    /**
     * Create a new statement list
     */
    Statements() {
        this.statements = new ArrayList<>();
    }

    /**
     * Adds a statement to this list
     *
     * @param s the statement to add
     * @return this for chained calls
     */
    public Statements add(Statement s) {
        statements.add(s);
        return this;
    }

    @Override
    public void execute(Context context) throws HGSEvalException {
        for (Statement s : statements)
            s.execute(context);
    }

    /**
     * If the list contains only on entry this entry is returned.
     * Otherwise this is returned.
     *
     * @return optimized statement
     */
    public Statement optimize() {
        if (statements.size() == 1)
            return statements.get(0);
        else
            return this;
    }
}
