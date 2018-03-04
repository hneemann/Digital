/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import java.util.*;

/**
 */
public class ContextFiller extends ContextMap implements Iterable<Variable> {

    private final List<Variable> vars;
    private final int rowCount;
    private final BitSetter bitSetter;

    /**
     * Creates a new instance.
     * The needed variables are taken from the expression
     *
     * @param expression the expression to extravt the variables
     */
    public ContextFiller(Expression expression) {
        this(new ArrayList<>(expression.traverse(new VariableVisitor()).getVariables()));
        Collections.sort(vars);
    }

    /**
     * Creates a new instance
     *
     * @param variables the variables to use
     */
    public ContextFiller(Variable... variables) {
        this(Arrays.asList(variables));
    }

    /**
     * Creates a new instance
     *
     * @param variables the variables to use
     */
    public ContextFiller(List<Variable> variables) {
        vars = variables;
        rowCount = 1 << vars.size();
        bitSetter = new BitSetter(vars.size()) {
            @Override
            public void setBit(int row, int i, boolean value) {
                set(vars.get(i), value);
            }
        };
    }

    @Override
    public Iterator<Variable> iterator() {
        return vars.iterator();
    }

    /**
     * Returns the variable with the given index
     *
     * @param index the index
     * @return the variable
     */
    public Variable getVar(int index) {
        return vars.get(index);
    }

    /**
     * @return the number of variables
     */
    public int getVarCount() {
        return vars.size();
    }

    /**
     * @return the number of tablerows to describe all variable combinations
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Fills the context with the given rows bit value
     *
     * @param bitValue the bit value, in most cases the tables row
     * @return this for call chaining
     */
    public ContextFiller setContextTo(int bitValue) {
        bitSetter.fill(bitValue);
        return this;
    }

    /**
     * @return the variables to use
     */
    public List<Variable> getVariables() {
        return vars;
    }
}
