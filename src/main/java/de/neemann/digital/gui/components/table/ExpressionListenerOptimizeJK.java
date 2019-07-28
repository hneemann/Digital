/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.DetermineJKStateMachine;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;

/**
 * If there are more than one solution for a truth table expression,
 * this Listener checks all for the complexity of the JK expressions.
 * The solution with the simplest JK expressions is chosen and reported
 * to the parent listener. All other expressions are discarded.
 * <p>
 * Created by helmut.neemann on 01.12.2016.
 */
public class ExpressionListenerOptimizeJK implements ExpressionListener {

    private final ExpressionListener parent;
    private String lastName;
    private Expression lastExpression;
    private int lastComplexity;

    /**
     * Created a new instance
     *
     * @param parent the parent expresson listener
     */
    public ExpressionListenerOptimizeJK(ExpressionListener parent) {
        this.parent = parent;
    }

    @Override
    public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
        String varName = ExpressionListenerJK.isSequentialVar(name);
        if (varName != null) {
            if (name.equals(lastName)) {
                int c = new DetermineJKStateMachine(varName, expression).getComplexity();
                if (c < lastComplexity) {
                    lastName = name;
                    lastExpression = expression;
                    lastComplexity = c;
                }
            } else {
                handlePending();
                lastName = name;
                lastExpression = expression;
                lastComplexity = new DetermineJKStateMachine(varName, expression).getComplexity();
            }
        } else {
            handlePending();
            parent.resultFound(name, expression);
        }
    }

    private void handlePending() throws FormatterException, ExpressionException {
        if (lastName != null) {
            parent.resultFound(lastName, lastExpression);
            lastName = null;
        }
    }


    @Override
    public void close() throws FormatterException, ExpressionException {
        handlePending();
        parent.close();
    }

}
