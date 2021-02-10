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

public class ExpressionListenerLogicFriday implements ExpressionListener {
    private final ArrayList<Result> results;
    private final HashSet<String> names;
    private VariableVisitor variables;
    private StringBuilder str;

    public ExpressionListenerLogicFriday() {
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

    private static class Result {
        private final String name;
        private final Expression expression;
        private final int number;

        public Result(String name, Expression expression, int number) {
            this.name = name;
            this.expression = expression;
            this.number = number;
        }

        public void createString(StringBuilder sb, Collection<Variable> variables, int results) throws ExpressionException {
            if (expression instanceof Operation.Or) {
                ArrayList<Expression> o = ((Operation.Or) expression).getExpressions();
                for (Expression e : o)
                    add(sb, e, variables, results);
            } else if (expression instanceof Operation.And)
                add(sb, expression, variables, results);
            else if (expression instanceof Variable)
                add(sb, Operation.and(expression), variables, results);
            else if (expression instanceof Not)
                add(sb, Operation.and(expression), variables, results);
            else
                throw new ExpressionException("invalid expression");
        }

        private void add(StringBuilder sb, Expression and, Collection<Variable> variables, int results) throws ExpressionException {
            HashSet<String> v = new HashSet<>();
            HashSet<String> nv = new HashSet<>();
            if (and instanceof Operation.And) {
                Operation.And a = (Operation.And) and;
                for (Expression var : a.getExpressions()) {
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
