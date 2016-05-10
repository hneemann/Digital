package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;

/**
 * @author hneemann
 */
public interface ExpressionModifier {
    ExpressionModifier IDENTITY = expression -> expression;

    Expression modify(Expression expression);

}
