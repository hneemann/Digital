/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import de.neemann.digital.analyse.expression.modify.ExpressionModifier;

/**
 * An expression which can be evaluated to a boolean value
 */
public interface Expression {

    /**
     * Evaluates the expression and returns the bool value
     *
     * @param context the expressions context
     * @return the bool value
     * @throws ExpressionException ExpressionException
     */
    boolean calculate(Context context) throws ExpressionException;

    /**
     * Traverses the expression
     *
     * @param visitor the visitor
     * @param <V>     the visitors type
     * @return the visitor
     */
    <V extends ExpressionVisitor> V traverse(V visitor);

    /**
     * Used to modify the ast.
     * Don't call this method directly!
     * Use {@link ExpressionModifier#modifyExpression(Expression, ExpressionModifier)} instead!
     *
     * @param modifier the modifier
     */
    default void modify(ExpressionModifier modifier) {
    }

    /**
     * String used to order expressions
     *
     * @return the ordering string
     */
    String getOrderString();

    /**
     * @return a deep copy of this expression
     */
    Expression copy();
}
