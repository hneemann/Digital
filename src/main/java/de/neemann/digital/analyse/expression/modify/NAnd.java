/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;

/**
 */
public class NAnd implements ExpressionModifier {

    @Override
    public Expression modify(Expression expression) {
        if (expression instanceof Operation.And) {
            return not(new Not(expression).protect());
        } else if (expression instanceof Operation.Or) {
            ArrayList<Expression> exp = new ArrayList<>();
            for (Expression e : ((Operation.Or) expression).getExpressions())
                exp.add(not(e));
            return new Not(Operation.and(exp)).protect();
        } else
            return expression;
    }
}
