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
public class IfStatement extends Statement {
    private final Expr condition;
    private final Statement trueStmt;
    private final Statement falseStmt;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param condition the condition
     * @param trueStmt the statement to execute if the condition is true
     * @param falseStmt the statement to execute if the condition is false
     */
    public IfStatement(int line, Expr condition, Statement trueStmt, Statement falseStmt) {
        super(line);
        this.condition = condition;
        this.trueStmt = trueStmt;
        this.falseStmt = falseStmt;
    }

    /**
     * Returns the condition
     *
     * @return the condition
     */
    public Expr getCondition() {
        return condition;
    }

    /**
     * Returns the statement to execute if the condition is true
     *
     * @return the statement
     */
    public Statement getTrueStmt() {
        return trueStmt;
    }

    /**
     * Returns the statement to execute if the condition is false
     *
     * @return the statement
     */
    public Statement getFalseStmt() {
        return falseStmt;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        RtValue objValue = condition.evaluate(ctx);

        if (!(objValue instanceof IntValue)) {
            throw new HGSException(getLine(), "Condition should be integer.");
        }
        int condValue = ((IntValue) objValue).getValue();

        if (condValue != 0) {
            trueStmt.execute(ctx);
        } else if (falseStmt != null) {
            falseStmt.execute(ctx);
        }
    }
}
