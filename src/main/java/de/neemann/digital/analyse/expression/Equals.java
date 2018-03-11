/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Checks if two expressions are equal.
 * Is an expensive operation with O(2^numVars). Use with care!
 */
public class Equals {

    private final Expression a;
    private final Expression b;

    /**
     * Creates a new instance
     *
     * @param a first expression
     * @param b second expression
     */
    public Equals(Expression a, Expression b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @return true if expressions are equal
     * @throws ExpressionException error calculating expression
     */
    public boolean isEqual() throws ExpressionException {
        VariableVisitor vva = a.traverse(new VariableVisitor());
        VariableVisitor vvb = b.traverse(new VariableVisitor());

        Collection<Variable> variables = vva.getVariables();
        if (!variables.equals(vvb.getVariables()))
            return false;

        ArrayList<Variable> vars = new ArrayList<>(variables.size());
        vars.addAll(variables);

        ContextFiller cf = new ContextFiller(vars);
        int count = 1 << vars.size();
        for (int i = 0; i < count; i++) {
            cf.setContextTo(i);
            if (a.calculate(cf) != b.calculate(cf))
                return false;
        }
        return true;
    }


}
