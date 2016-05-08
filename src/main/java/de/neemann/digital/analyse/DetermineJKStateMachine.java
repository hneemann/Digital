package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.lang.Lang;

import java.util.Iterator;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * Creates the expressions to create a JK-FF state machine
 *
 * @author hneemann
 */
public class DetermineJKStateMachine {
    private Expression j = null;
    private Expression nk = null;

    /**
     * Creates a new instance
     *
     * @param name the name of the state variable
     * @param e    the expression to split in J and K expression
     * @throws ExpressionException ExpressionException
     * @throws FormatterException  FormatterException
     */
    public DetermineJKStateMachine(String name, Expression e) throws ExpressionException, FormatterException {
        String notName = "¬" + name;

        boolean wasK = false;
        boolean wasJ = false;
        for (Expression or : getOrs(e)) {

            Expression term = null;
            boolean belongsToK = false;
            boolean belongsToJ = false;

            for (Expression a : getAnds(or)) {
                String str = FormatToExpression.FORMATTER_UNICODE.format(a);
                if (str.equals(name)) {
                    belongsToK = true;
                    wasK = true;
                } else if (str.equals(notName)) {
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
                    j = or(term, j);
                } else if (belongsToK) {
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
