package de.neemann.digital.analyse.expression.modify;

import de.neemann.digital.analyse.expression.Expression;

/**
 * Interface to implement an expression modifier
 *
 * @author hneemann
 */
public interface ExpressionModifier {
    /**
     * The identity modification
     */
    ExpressionModifier IDENTITY = expression -> expression;

    /**
     * Modifies the given expression with the given modifier
     *
     * @param expression the expression to modify
     * @param modifier   the modifier to use
     * @return the modified expression
     */
    static Expression modifyExpression(Expression expression, ExpressionModifier modifier) {
        expression.modify(modifier);
        return modifier.modify(expression);
    }

    /**
     * Modifies the given expression with the given modifiers
     *
     * @param expression the expression to modify
     * @param modifiers  the modifiers to use
     * @return the modified expression
     */
    static Expression modifyExpression(Expression expression, ExpressionModifier... modifiers) {
        for (ExpressionModifier m : modifiers)
            expression = modifyExpression(expression, m);
        return expression;
    }

    /**
     * Modifies an expression.
     * Don't recurse! Recursion is done by calling {@link Expression#modify(ExpressionModifier)}
     * Don't call this method directly. Call {@link ExpressionModifier#modifyExpression(Expression, ExpressionModifier)} instead!
     *
     * @param expression the expression to modify
     * @return the modified expression
     */
    Expression modify(Expression expression);

}
