/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Operation;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Operation.andNoMerge;
import static de.neemann.digital.analyse.expression.Operation.orNoMerge;

/**
 */
public class TwoInputs implements ExpressionModifier {
    @Override
    public Expression modify(Expression expression) {
        if (expression instanceof Operation) {
            Operation op = (Operation) expression;
            if (op.getExpressions().size() > 2) {
                if (expression instanceof Operation.And)
                    return new Twoer(op.getExpressions(), (a, b) -> andNoMerge(a, b)).generate();
                else if (expression instanceof Operation.Or)
                    return new Twoer(op.getExpressions(), (a, b) -> orNoMerge(a, b)).generate();
                else
                    throw new RuntimeException("operation not supported: " + expression.getClass().getSimpleName());
            } else
                return expression;
        } else
            return expression;
    }

    private interface OpGen {
        Expression op(Expression a, Expression b);
    }

    private static final class Twoer {
        private final ArrayList<Expression> expressions;
        private final OpGen opGen;

        Twoer(ArrayList<Expression> expressions, OpGen opGen) {
            this.expressions = expressions;
            this.opGen = opGen;
        }

        Expression generate() {
            return gen(0, expressions.size() - 1);
        }

        private Expression gen(int a, int b) {
            if (a == b)
                return expressions.get(a);
            else if (a == b - 1)
                return opGen.op(expressions.get(a), expressions.get(b));
            else {
                int i = (a + b) / 2;
                return opGen.op(gen(a, i), gen(i + 1, b));
            }
        }

    }

}
