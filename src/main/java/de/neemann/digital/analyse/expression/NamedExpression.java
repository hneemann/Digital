/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

/**
 */
public class NamedExpression implements Expression {

    private final Expression exp;
    private final String name;

    /**
     * Creates a new instance
     *
     * @param name the name of the expression
     * @param exp  the expression
     */
    public NamedExpression(String name, Expression exp) {
        this.exp = exp;
        this.name = name;
    }

    /**
     * @return the name of the expression
     */
    public String getName() {
        return name;
    }

    /**
     * @return the named expression
     */
    public Expression getExpression() {
        return exp;
    }


    @Override
    public boolean calculate(Context context) throws ExpressionException {
        return exp.calculate(context);
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V visitor) {
        return exp.traverse(visitor);
    }

    @Override
    public String getOrderString() {
        return exp.getOrderString();
    }

    @Override
    public Expression copy() {
        return new NamedExpression(name, exp.copy());
    }

    @Override
    public String toString() {
        return name+"="+exp.toString();
    }
}
