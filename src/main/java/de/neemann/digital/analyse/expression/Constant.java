/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

/**
 * A constant
 */
public final class Constant implements Expression {
    private final boolean value;

    /**
     * The constant true or one
     */
    public static final Constant ONE = new Constant(true);

    /**
     * The constant false or zero
     */
    public static final Constant ZERO = new Constant(false);

    private Constant(boolean value) {
        this.value = value;
    }

    @Override
    public boolean calculate(Context context) {
        return value;
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        v.visit(this);
        return v;
    }

    @Override
    public String getOrderString() {
        return Boolean.toString(value);
    }

    /**
     * Returns the constant value
     *
     * @return the value
     */
    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public Expression copy() {
        return this;
    }
}
