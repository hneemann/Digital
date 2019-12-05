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
 * Expression listener which sends all result to its parent.
 * If the expression is a state expression it creates the J and K control equations
 * and also sends the calculated control expressions to its parent.
 */
public class ExpressionListenerJK implements ExpressionListener {
    private final ExpressionListener parent;

    /**
     * Creates a new instance
     *
     * @param parent the parent ExpressionListener
     */
    public ExpressionListenerJK(ExpressionListener parent) {
        this.parent = parent;
    }

    @Override
    public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
        parent.resultFound(name, expression);

        String detName = isSequentialVar(name);

        if (detName != null) {
            DetermineJKStateMachine jk = new DetermineJKStateMachine(detName, expression);

            if (detName.endsWith("^n"))
                detName = detName.substring(0, detName.length() - 2);
            else
                detName = detName.substring(0, detName.length() - 1);
            Expression j = jk.getJ();
            parent.resultFound("J_" + detName, j);
            Expression s = jk.getSimplifiedJ();
            if (!s.toString().equals(j.toString())) {
                parent.resultFound("", s);
            }
            Expression k = jk.getK();
            parent.resultFound("K_" + detName, k);
            s = jk.getSimplifiedK();
            if (!s.toString().equals(k.toString())) {
                parent.resultFound("", s);
            }
        }

    }

    /**
     * If the name belongs to a sequential state var, the state vars name is returned.
     * Otherwise a null is returned
     *
     * @param name the name of the variable
     * @return the state variable or null
     */
    public static String isSequentialVar(String name) {
        String detName = null;
        if (name.endsWith("n+1")) {
            detName = name.substring(0, name.length() - 2);
        } else if (name.endsWith("{n+1}")) {
            detName = name.substring(0, name.length() - 5) + "n";
        }
        return detName;
    }

    @Override
    public void close() throws FormatterException, ExpressionException {
        parent.close();
    }
}
