/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;


import de.neemann.digital.hdl.model2.HDLNet;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Represents a NOT operation
 */
public class ExprNot implements Expression {

    private Expression expr;

    /**
     * Creates a new NOT expression
     *
     * @param expr the enxpression to invert
     */
    public ExprNot(Expression expr) {
        this.expr = expr;
    }

    /**
     * @return the expression
     */
    public Expression getExpression() {
        return expr;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print("NOT ");
        expr.print(out);
    }

    @Override
    public void replace(HDLNet net, Expression expression) {
        if (Expression.isVar(expr, net))
            expr = expression;
        else
            expr.replace(net, expression);
    }

    @Override
    public void traverse(Visitor visitor) {
        visitor.visit(this);
        expr.traverse(visitor);
    }

    @Override
    public void optimize(ExpressionOptimizer eo) {
        expr = eo.optimize(expr);
        expr.optimize(eo);
    }

    /**
     * Optimizes not(not a) expressions to a
     */
    public static class OptimizeNotNot implements ExpressionOptimizer {
        @Override
        public Expression optimize(Expression expression) {
            if (expression instanceof ExprNot) {
                ExprNot not = (ExprNot) expression;
                if (not.expr instanceof ExprNot)
                    return optimize(((ExprNot) not.expr).expr);
            }
            return expression;
        }
    }
}
