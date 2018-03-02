/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Visitor to collect all used variables in an expression
 */
public class VariableVisitor implements ExpressionVisitor {

    private final TreeSet<Variable> variables;

    /**
     * Creates a new instance
     */
    public VariableVisitor() {
        variables = new TreeSet<>();
    }

    @Override
    public boolean visit(Expression expression) {
        if (expression instanceof Variable) {
            variables.add((Variable) expression);
        }
        return true;
    }

    /**
     * Returns all used variables
     *
     * @return used variables
     */
    public Collection<Variable> getVariables() {
        return variables;
    }
}
