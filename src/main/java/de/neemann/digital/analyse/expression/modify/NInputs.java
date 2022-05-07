/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Operation;

import java.util.ArrayList;

/**
 * uses only gates with three inputs
 */
public class NInputs implements ExpressionModifier {

    private final int inputs;

    /**
     * Creates a new instance
     *
     * @param inputs the max number of inputs to use
     */
    public NInputs(int inputs) {
        this.inputs = inputs;
    }

    @Override
    public Expression modify(Expression expression) {
        if (expression instanceof Operation) {
            Operation op = (Operation) expression;
            if (op.getExpressions().size() > inputs) {
                if (expression instanceof Operation.And)
                    return generate(op.getExpressions(), Operation::andNoMerge);
                else if (expression instanceof Operation.Or)
                    return generate(op.getExpressions(), Operation::orNoMerge);
            }
        }
        return expression;
    }

    private interface OpGen {
        Expression op(Expression... ex);
    }

    private Expression generate(ArrayList<Expression> list, OpGen op) {
        if (list.size() <= inputs) {
            return op.op(list.toArray(new Expression[0]));
        } else {
            ArrayList<Expression> resList = new ArrayList<>();

            int desiredSize = inputs;
            int res = list.size() % inputs;
            if (res != 0)
                desiredSize = (res + inputs) / 2;

            ArrayList<Expression> iList = new ArrayList<>();
            for (Expression e : list) {
                iList.add(e);
                if (iList.size() == desiredSize) {
                    resList.add(op.op(iList.toArray(new Expression[0])));
                    desiredSize = inputs;
                    iList.clear();
                }
            }
            if (iList.size() > 0)
                resList.add(op.op(iList.toArray(new Expression[0])));

            return generate(resList, op);
        }
    }

}
