/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;


import de.neemann.digital.hdl.model2.HDLNet;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represenst a operation
 */
public class ExprOperate implements Expression {
    /**
     * the possible operation
     */
    public enum Operation {
        /**
         * And operation
         */
        AND,
        /**
         * Or operation
         */
        OR,
        /**
         * xor operation
         */
        XOR
    }

    private Operation operation;
    private ArrayList<Expression> operands;

    /**
     * Creates a new instance
     *
     * @param operation the operation
     * @param operands  the operandes
     */
    public ExprOperate(Operation operation, ArrayList<Expression> operands) {
        this.operation = operation;
        this.operands = operands;
    }

    /**
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * @return the operands
     */
    public ArrayList<Expression> getOperands() {
        return operands;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print("(");
        boolean first = true;
        for (Expression op : operands) {
            if (first)
                first = false;
            else
                out.print(" ").print(operation.name()).print(" ");
            op.print(out);
        }
        out.print(")");
    }

    @Override
    public void replace(HDLNet net, Expression expression) {
        for (int i = 0; i < operands.size(); i++) {
            final Expression op = operands.get(i);
            if (Expression.isVar(op, net))
                operands.set(i, expression);
            else
                op.replace(net, expression);
        }
    }

    @Override
    public void traverse(Visitor visitor) {
        visitor.visit(this);
        for (Expression o : operands)
            o.traverse(visitor);
    }

    @Override
    public void optimize(ExpressionOptimizer eo) {
        for (int i = 0; i < operands.size(); i++) {
            Expression expr = eo.optimize(operands.get(i));
            expr.optimize(eo);
            operands.set(i, expr);
        }
    }
}
