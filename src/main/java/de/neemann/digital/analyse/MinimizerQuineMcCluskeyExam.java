/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.ComplexityVisitor;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.QuineMcCluskeyExam;
import de.neemann.digital.gui.components.table.ExpressionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The QMC-Minimizer used for exam correction.
 * Should only be used if there are not more than 4 variables.
 */
public class MinimizerQuineMcCluskeyExam extends MinimizerQuineMcCluskey {

    @Override
    public void minimize(List<Variable> vars, BoolTable boolTable, String resultName, ExpressionListener listener) throws ExpressionException, FormatterException {
        ExpressionListenerEnsureMinimal l = new ExpressionListenerEnsureMinimal(listener);
        super.minimize(vars, boolTable, resultName, l);
        l.close();
    }

    @Override
    protected QuineMcCluskey createQuineMcCluskey(List<Variable> vars) {
        return new QuineMcCluskeyExam(vars);
    }

    /**
     * This minimizer only ensures there is a minimal number of product terms. Not that theses product
     * terms have a minimal number of variables.
     * This filter ensures, that the results are of minimal complexity
     */
    private static final class ExpressionListenerEnsureMinimal implements ExpressionListener {
        private final ExpressionListener parent;
        private String lastName;
        private ArrayList<Expression> list;
        private int complexity;

        private ExpressionListenerEnsureMinimal(ExpressionListener parent) {
            this.parent = parent;
            list = new ArrayList<>();
        }

        @Override
        public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
            if (!name.equals(lastName)) {
                for (Expression e : list)
                    parent.resultFound(lastName, e);

                list.clear();
                complexity = Integer.MAX_VALUE;
                lastName = name;
            }
            int comp = expression.traverse(new ComplexityVisitor()).getComplexity();
            if (comp < complexity) {
                list.clear();
                complexity = comp;
            }

            list.add(expression);
        }

        @Override
        public void close() throws FormatterException, ExpressionException {
            for (Expression e : list)
                parent.resultFound(lastName, e);

            // do not close the parent!!!
        }
    }

}
