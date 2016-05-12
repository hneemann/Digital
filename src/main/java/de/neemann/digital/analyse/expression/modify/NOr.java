package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;

/**
 * @author hneemann
 */
public class NOr implements ExpressionModifier {

    @Override
    public Expression modify(Expression expression) {
        if (expression instanceof Operation.Or) {
            return not(new Not(expression).protect());
        } else
        if (expression instanceof Operation.And) {
            ArrayList<Expression> exp = new ArrayList<>();
            for (Expression e : ((Operation.And) expression).getExpressions())
                exp.add(not(e));
            return new Not(Operation.or(exp)).protect();
        } else
            return expression;
    }
}
