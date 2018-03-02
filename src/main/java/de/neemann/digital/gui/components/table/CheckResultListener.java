/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.lang.Lang;

import java.util.List;

/**
 * Checks the created results
 */
public class CheckResultListener implements ExpressionListener {
    private final ExpressionListener listener;
    private final List<Variable> variables;
    private final BoolTable boolTable;

    /**
     * Creates a new instance
     *
     * @param listener  the listener to delegate the results to
     * @param variables the variables used
     * @param boolTable the booltable to check
     * @throws ExpressionException ExpressionException
     */
    public CheckResultListener(ExpressionListener listener, List<Variable> variables, BoolTable boolTable) throws ExpressionException {
        this.listener = listener;
        this.variables = variables;
        this.boolTable = boolTable;

        int n = 1 << variables.size();
        if (n != boolTable.size())
            throw new ExpressionException(Lang.get("err_exact_N0_valuesNecessaryNot_N1", n, boolTable.size()));

    }

    @Override
    public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
        listener.resultFound(name, expression);

        ContextFiller cf = new ContextFiller(variables);

        for (int i = 0; i < boolTable.size(); i++)
            check(boolTable.get(i), expression.calculate(cf.setContextTo(i)));

    }

    private void check(ThreeStateValue threeStateValue, boolean calculate) throws ExpressionException {
        switch (threeStateValue) {
            case dontCare:
                return;
            case one:
                if (!calculate)
                    throw new ExpressionException(Lang.get("err_minimizationFailed"));
                break;
            case zero:
                if (calculate)
                    throw new ExpressionException(Lang.get("err_minimizationFailed"));
                break;
        }
    }

    @Override
    public void close() throws FormatterException, ExpressionException {
        listener.close();
    }
}
