/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import de.neemann.digital.analyse.expression.modify.ExpressionModifier;

import java.util.Objects;

/**
 */
public final class Not implements Expression {

    private Expression expression;
    private boolean protect = false;

    /**
     * Creates a not expression.
     * Simplifies the expression if possible.
     *
     * @param a the child expression to invert
     * @return the inverted expression
     */
    public static Expression not(Expression a) {
        if (a == Constant.ONE)
            return Constant.ZERO;
        if (a == Constant.ZERO)
            return Constant.ONE;

        if (a instanceof Not && !((Not) a).protect) {
            return ((Not) a).expression;
        } else
            return new Not(a);
    }

    /**
     * Creates a new instance.
     * In most cases it's better to use {@link Not#not(Expression)}.
     *
     * @param expression the expression to invert
     */
    public Not(Expression expression) {
        this.expression = expression;
    }

    /**
     * Protects this not against simplification.
     * So it is possible to create a NAnd: You can create an And and protect the outer Not.
     * No simplification will take place in this case and you will end up with a NAnd gate.
     *
     * @return this for call chaining
     */
    public Not protect() {
        protect = true;
        return this;
    }

    /**
     * @return true if this not is protected.
     */
    public boolean isProtected() {
        return protect;
    }

    @Override
    public boolean calculate(Context context) throws ExpressionException {
        return !expression.calculate(context);
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        if (v.visit(this)) {
            expression.traverse(v);
        }
        return v;
    }

    @Override
    public void modify(ExpressionModifier modifier) {
        expression.modify(modifier);
        expression = modifier.modify(expression);
    }

    @Override
    public String getOrderString() {
        return expression.getOrderString();
    }

    @Override
    public Expression copy() {
        return new Not(expression.copy());
    }

    /**
     * @return the negated expression
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "not(" + expression + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Not not = (Not) o;
        return expression.equals(not.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
