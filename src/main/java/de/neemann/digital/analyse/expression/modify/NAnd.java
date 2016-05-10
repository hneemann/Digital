package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;

/**
 * @author hneemann
 */
public class NAnd implements ExpressionModifier {

    @Override
    public Expression modify(Expression expression) {
        if (expression instanceof Operation.And) {
            return new Not(not(expression));
        } else if (expression instanceof Operation.Or) {
            ArrayList<Expression> exp = new ArrayList<>();
            for (Expression e : ((Operation.Or) expression).getExpressions())
                exp.add(not(e));
            return not(Operation.and(exp));
        } else
            return expression;
    }
}
