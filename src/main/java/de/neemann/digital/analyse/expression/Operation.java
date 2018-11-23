/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import de.neemann.digital.analyse.expression.modify.ExpressionModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A operation
 * There are only two implementations: The AND and the OR operation
 */
public abstract class Operation implements Expression {
    private static final Comparator<Expression> EXPRESSION_COMPARATOR
            = Comparator.comparing(Expression::getOrderString);

    private final ArrayList<Expression> expr;

    /**
     * Creates a new OR expression
     *
     * @param exp the expressions to OR
     * @return the created expression
     */
    public static Expression or(Iterable<Expression> exp) {
        return simplify(new Or(exp, true));
    }

    /**
     * Creates a new OR expression
     *
     * @param exp the expressions to OR
     * @return the created expression
     */
    public static Expression or(Expression... exp) {
        return simplify(new Or(Arrays.asList(exp), true));
    }

    /**
     * Creates a new XOR expression
     *
     * @param a the expression to XOR
     * @param b the expression to XOR
     * @return the created expression
     */
    public static Expression xor(Expression a, Expression b) {
        if (b == Constant.ONE) {
            return Not.not(a);
        } else if (b == Constant.ZERO) {
            return a;
        } else if (a == Constant.ONE) {
            return Not.not(b);
        } else if (a == Constant.ZERO) {
            return b;
        } else
            return simplify(new XOr(a, b));
    }


    /**
     * Creates a new OR expression
     *
     * @param exp the expressions to OR
     * @return the created expression
     */
    public static Expression orNoMerge(Expression... exp) {
        return simplify(new Or(Arrays.asList(exp), false));
    }

    /**
     * Creates a new AND expression
     *
     * @param exp the expressions to AND
     * @return the created expression
     */
    public static Expression and(Iterable<Expression> exp) {
        return simplify(new And(exp, true));
    }

    /**
     * Creates a new AND expression
     *
     * @param exp the expressions to AND
     * @return the created expression
     */
    public static Expression and(Expression... exp) {
        return simplify(new And(Arrays.asList(exp), true));
    }

    /**
     * Creates a new AND expression
     *
     * @param exp the expressions to AND
     * @return the created expression
     */
    public static Expression andNoMerge(Expression... exp) {
        return simplify(new And(Arrays.asList(exp), false));
    }

    private static Expression simplify(Operation operation) {
        int size = operation.getExpressions().size();
        switch (size) {
            case 0:
                return null;
            case 1:
                return operation.getExpressions().get(0);
            default:
                operation.getExpressions().sort(EXPRESSION_COMPARATOR);
                return operation;
        }
    }

    private Operation(Iterable<Expression> exp, boolean merge) {
        expr = new ArrayList<>();
        for (Expression e : exp)
            if (e != null)
                if (merge)
                    merge(e);
                else
                    expr.add(e);
    }

    private Operation(Iterable<Expression> expToCopy) {
        expr = new ArrayList<>();
        for (Expression e : expToCopy)
            if (e != null)
                expr.add(e.copy());
    }

    private void merge(Expression e) {
        if (e.getClass() == getClass()) {
            expr.addAll(((Operation) e).getExpressions());
        } else
            expr.add(e);
    }

    @Override
    public boolean calculate(Context context) throws ExpressionException {
        boolean result = getNeutral();
        for (Expression e : expr)
            result = calc(result, e.calculate(context));
        return result;
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        if (v.visit(this)) {
            for (Expression e : expr)
                e.traverse(v);
        }
        return v;
    }

    @Override
    public void modify(ExpressionModifier modifier) {
        for (int i = 0; i < expr.size(); i++) {
            Expression e = expr.get(i);
            e.modify(modifier);
            expr.set(i, modifier.modify(e));
        }
    }

    /**
     * @return the sub expressions
     */
    public ArrayList<Expression> getExpressions() {
        return expr;
    }

    @Override
    public String getOrderString() {
        return expr.get(0).getOrderString();
    }

    /**
     * @return the neutral element of this operation
     */
    protected abstract boolean getNeutral();

    /**
     * Performs the calculation
     *
     * @param a value a
     * @param b value b
     * @return result
     */
    protected abstract boolean calc(boolean a, boolean b);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (Expression e : expr) {
            if (sb.length() > 1)
                sb.append(",");
            sb.append(e.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * The AND expression
     */
    public static final class And extends Operation {

        private And(Iterable<Expression> exp, boolean merge) {
            super(exp, merge);
        }

        private And(Iterable<Expression> expToCopy) {
            super(expToCopy);
        }

        @Override
        protected boolean getNeutral() {
            return true;
        }

        @Override
        protected boolean calc(boolean a, boolean b) {
            return a && b;
        }

        @Override
        public String toString() {
            return "and" + super.toString();
        }

        @Override
        public Expression copy() {
            return new And(getExpressions());
        }
    }

    /**
     * The OR expression
     */
    public static final class Or extends Operation {

        private Or(Iterable<Expression> exp, boolean merge) {
            super(exp, merge);
        }

        private Or(Iterable<Expression> expToCopy) {
            super(expToCopy);
        }

        @Override
        protected boolean getNeutral() {
            return false;
        }

        @Override
        protected boolean calc(boolean a, boolean b) {
            return a || b;
        }

        @Override
        public String toString() {
            return "or" + super.toString();
        }

        @Override
        public Expression copy() {
            return new Or(getExpressions());
        }
    }

    /**
     * The XOR expression
     */
    public static final class XOr extends Operation {

        private XOr(Expression a, Expression b) {
            super(Arrays.asList(a, b), false);
        }

        private XOr(Iterable<Expression> expToCopy) {
            super(expToCopy);
        }

        @Override
        protected boolean getNeutral() {
            return false;
        }

        @Override
        protected boolean calc(boolean a, boolean b) {
            return (a || b) && (!a || !b);
        }

        @Override
        public String toString() {
            return "xor" + super.toString();
        }

        @Override
        public Expression copy() {
            return new XOr(getExpressions());
        }
    }

}
