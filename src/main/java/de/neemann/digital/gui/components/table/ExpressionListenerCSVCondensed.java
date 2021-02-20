/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.expression.format.FormatterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Exports a CSV table containing only the prime implicants
 */
public class ExpressionListenerCSVCondensed implements ExpressionListener {
    private final ArrayList<Result> results;
    private final HashSet<String> names;
    private final VariableVisitor variables;
    private StringBuilder str;

    /**
     * Creates a new instance
     */
    public ExpressionListenerCSVCondensed() {
        results = new ArrayList<>();
        names = new HashSet<>();
        variables = new VariableVisitor();
    }

    @Override
    public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
        if (!names.contains(name)) {
            names.add(name);
            results.add(new Result(name, expression, results.size()));
            expression.traverse(variables);
        }
    }

    @Override
    public void close() throws FormatterException, ExpressionException {
        str = new StringBuilder();
        for (Variable var : variables.getVariables())
            str.append(var.getIdentifier()).append(",");
        for (Result r : results)
            str.append(",").append(r.name);
        str.append("\n");

        for (Result r : results)
            r.createString(str, variables.getVariables(), results.size());
    }

    @Override
    public String toString() {
        return str.toString();
    }

    private static final class Result {
        private final String name;
        private final Expression expression;
        private final int number;

        private Result(String name, Expression expression, int number) {
            this.name = name;
            this.expression = expression;
            this.number = number;
        }

        public void createString(StringBuilder sb, Collection<Variable> variables, int results) throws ExpressionException {
            if (expression instanceof Operation.Or) {
                ArrayList<Expression> o = ((Operation.Or) expression).getExpressions();
                for (Expression e : o)
                    addPrime(sb, e, variables, results);
            } else
                addPrime(sb, expression, variables, results);
        }

        private void addPrime(StringBuilder sb, Expression and, Collection<Variable> variables, int results) throws ExpressionException {
            if (and instanceof Operation.And)
                addAnd(sb, and, variables, results);
            else if (and instanceof Variable)
                addVar(sb, ((Variable) and).getIdentifier(), variables, results, false);
            else if (and instanceof Not && ((Not) and).getExpression() instanceof Variable)
                addVar(sb, ((Variable) (((Not) and).getExpression())).getIdentifier(), variables, results, true);
            else
                throw new ExpressionException("invalid expression");
        }

        private void addVar(StringBuilder sb, String identifier, Collection<Variable> variables, int results, boolean invert) {
            for (Variable var : variables) {
                if (var.getIdentifier().endsWith(identifier)) {
                    if (invert)
                        sb.append("0,");
                    else
                        sb.append("1,");
                } else
                    sb.append("X,");
            }

            for (int i = 0; i < results; i++) {
                if (i == number)
                    sb.append(",1");
                else
                    sb.append(",0");
            }
            sb.append('\n');
        }

        private void addAnd(StringBuilder sb, Expression and, Collection<Variable> variables, int results) throws ExpressionException {
            HashSet<String> v = new HashSet<>();
            HashSet<String> nv = new HashSet<>();
            if (and instanceof Operation.And) {
                Operation.And a = (Operation.And) and;
                for (Expression va : a.getExpressions()) {
                    Expression var = va;
                    HashSet<String> map = v;
                    if (var instanceof Not) {
                        map = nv;
                        var = ((Not) var).getExpression();
                    }
                    if (var instanceof Variable)
                        map.add(((Variable) var).getIdentifier());
                    else
                        throw new ExpressionException("invalid expression");
                }
            } else
                throw new ExpressionException("invalid expression");

            for (Variable var : variables) {
                if (v.contains(var.getIdentifier()))
                    sb.append("1,");
                else if (nv.contains(var.getIdentifier()))
                    sb.append("0,");
                else
                    sb.append("X,");
            }

            for (int i = 0; i < results; i++) {
                if (i == number)
                    sb.append(",1");
                else
                    sb.append(",0");
            }
            sb.append('\n');
        }
    }
}
