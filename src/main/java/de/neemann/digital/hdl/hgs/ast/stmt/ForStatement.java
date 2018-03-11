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
public class ForStatement extends Statement {
    private final Statement assignment;
    private final Expr condition;
    private final Statement stepStmt;
    private final Statement blockStmt;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param assignment the assignment statement
     * @param condition the condition
     * @param incStmt the increment statement
     * @param blockStmt the statement block
     */
    public ForStatement(int line, Statement assignment, Expr condition, Statement incStmt, Statement blockStmt) {
        super(line);
        this.assignment = assignment;
        this.condition = condition;
        this.stepStmt = incStmt;
        this.blockStmt = blockStmt;
    }

    /**
     * Return the assignment
     *
     * @return the assignment
     */
    public Statement getAssignment() {
        return assignment;
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
     * Returns the increment statement
     *
     * @return the increment statement
     */
    public Statement getIncStmt() {
        return stepStmt;
    }

    /**
     * Return the statement of the loop body
     *
     * @return the loop body statement
     */
    public Statement getBlockStmt() {
        return blockStmt;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        assignment.execute(ctx);
        RtValue objValue = condition.evaluate(ctx);

        if (!(objValue instanceof IntValue)) {
            throw new HGSException(getLine(), "Condition should be integer.");
        }
        int condValue = ((IntValue) objValue).getValue();

        while (condValue != 0) {
            blockStmt.execute(ctx);
            stepStmt.execute(ctx);
            condValue = ((IntValue) condition.evaluate(ctx)).getValue();
        }
    }
}
