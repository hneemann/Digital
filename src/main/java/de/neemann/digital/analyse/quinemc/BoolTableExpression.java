/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * Creates a bool table from the given expression
 */
public class BoolTableExpression implements BoolTable {
    private final Expression expression;
    private final ContextFiller context;

    /**
     * Creates a new instance
     *
     * @param expression the expression
     * @param context    the context to evaluate the expression
     */
    public BoolTableExpression(Expression expression, ContextFiller context) {
        this.expression = expression;
        this.context = context;
    }

    @Override
    public int size() {
        return 1 << context.getVarCount();
    }

    @Override
    public ThreeStateValue get(int i) {
        context.setContextTo(i);
        try {
            return ThreeStateValue.value(expression.calculate(context));
        } catch (ExpressionException e) {
            throw new RuntimeException(e); // ToDo!!
        }
    }
}
