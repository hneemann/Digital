/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.ast.expr.Expr;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtValue;

/**
 *
 * @author ideras
 */
public class WhileStatement extends Statement {
    private final Expr condition;
    private final Statement statement;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param condition the loop condition
     * @param statement the loop body statement
     */
    public WhileStatement(int line, Expr condition, Statement statement) {
        super(line);
        this.condition = condition;
        this.statement = statement;
    }

    /**
     * Returns the loop condition
     *
     * @return the loop condition
     */
    public Expr getCondition() {
        return condition;
    }

    /**
     * Returns the loop body statement
     *
     * @return the loop body statement
     */
    public Statement getStatement() {
        return statement;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        RtValue objValue = condition.evaluate(ctx);

        if (!(objValue instanceof IntValue)) {
            throw new HGSException(getLine(), "while loop condition should be integer.");
        }
        int condValue = ((IntValue) objValue).getValue();

        while (condValue != 0) {
            statement.execute(ctx);
            condValue = ((IntValue) condition.evaluate(ctx)).getValue();
        }
    }

}
