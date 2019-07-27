/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.lang.Lang;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * Creates the expressions to create a JK-FF state machine
 */
public class DetermineJKStateMachine {
    private Expression j = null;
    private Expression nk = null;
    private Expression simpj = null;
    private Expression simpk = null;
    private boolean isDFF;

    /**
     * Creates a new instance
     *
     * @param name the name of the state variable
     * @param e    the expression to split in J and K expression
     * @throws ExpressionException ExpressionException
     */
    public DetermineJKStateMachine(String name, Expression e) throws ExpressionException {
        final Expression var = new Variable(name);
        final Expression notVar = not(var);

        boolean wasK = false;
        boolean wasJ = false;
        for (Expression or : getOrs(e)) {

            Expression term = null;
            boolean belongsToK = false;
            boolean belongsToJ = false;

            for (Expression a : getAnds(or)) {
                if (a.equals(var)) {
                    belongsToK = true;
                    wasK = true;
                } else if (a.equals(notVar)) {
                    belongsToJ = true;
                    wasJ = true;
                } else {
                    term = and(term, a);
                }
            }

            if (belongsToJ && belongsToK) {
                throw new ExpressionException(Lang.get("err_containsVarAndNotVar"));
            } else {
                if (belongsToJ) {
                    if (term == null) term = Constant.ONE;
                    j = or(term, j);
                } else if (belongsToK) {
                    if (term == null) term = Constant.ONE;
                    nk = or(term, nk);
                } else {
                    j = or(term, j);
                    nk = or(term, nk);
                }
            }
        }
        if (j == null) {
            if (wasJ) j = Constant.ONE;
            else j = Constant.ZERO;
        }
        if (nk == null) {
            if (wasK) nk = Constant.ONE;
            else nk = Constant.ZERO;
        }
        isDFF = !wasJ && !wasK;
    }

    /**
     * @return returns a measure of the complexity of the JK expressions.
     * @throws ExpressionException ExpressionException
     */
    public int getComplexity() throws ExpressionException {
        return getSimplifiedJ().traverse(new ComplexityVisitor()).getComplexity()
                + getSimplifiedK().traverse(new ComplexityVisitor()).getComplexity();
    }

    private Iterable<Expression> getOrs(Expression e) {
        if (e instanceof Operation.Or) {
            return ((Operation.Or) e).getExpressions();
        } else
            return new AsIterable<>(e);
    }

    private Iterable<? extends Expression> getAnds(Expression e) {
        if (e instanceof Operation.And) {
            return ((Operation.And) e).getExpressions();
        } else
            return new AsIterable<>(e);
    }

    /**
     * @return the J expression
     */
    public Expression getJ() {
        return j;
    }

    /**
     * @return the not(K) expression
     */
    public Expression getNK() {
        return nk;
    }

    /**
     * @return the K expression
     */
    public Expression getK() {
        return not(nk);
    }

    /**
     * Returns true if it is logical a D flipflop.
     * This means that K = not(J).
     *
     * @return true if it is logical a D flipflop
     */
    public boolean isDFF() {
        return isDFF;
    }

    /**
     * @return simplified J
     * @throws ExpressionException ExpressionException
     */
    public Expression getSimplifiedJ() throws ExpressionException {
        if (simpj == null)
            simpj = QuineMcCluskey.simplify(getJ());
        return simpj;
    }

    /**
     * @return simplified K
     * @throws ExpressionException ExpressionException
     */
    public Expression getSimplifiedK() throws ExpressionException {
        if (simpk == null)
            simpk = QuineMcCluskey.simplify(getK());
        return simpk;
    }

    private static final class AsIterable<T> implements Iterable<T> {
        private final T item;

        private AsIterable(T item) {
            this.item = item;
        }

        @Override
        public Iterator<T> iterator() {
            return new SingleItemIterator<>(item);
        }

        private static final class SingleItemIterator<T> implements Iterator<T> {
            private T item;

            private SingleItemIterator(T item) {
                this.item = item;
            }

            @Override
            public boolean hasNext() {
                return item != null;
            }

            @Override
            public T next() {
                if (item == null)
                    throw new NoSuchElementException();
                T ee = item;
                item = null;
                return ee;
            }

            @Override
            public void remove() {
            }
        }
    }

}
