/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import java.util.ArrayList;

/**
 * Represents a variable as part of an expression.
 */
public class Variable implements Comparable<Variable>, Expression {

    private String identifier;

    /**
     * Creates a new Variable
     *
     * @param name the variables name
     * @return the new Variable
     */
    public static Variable v(String name) {
        return new Variable(name);
    }

    /**
     * Creates an array of variables
     *
     * @param n the number of varfiables to create.
     * @return variables named "A0", "A1", "A2" and so on
     */
    public static ArrayList<Variable> vars(int n) {
        ArrayList<Variable> v = new ArrayList<Variable>();
        for (int i = 0; i < n; i++)
            v.add(new Variable("" + (char) ('A' + i)));
        return v;
    }

    /**
     * Create a list of variables using the given names
     *
     * @param names the names used to create the variables
     * @return the list of variables
     */
    public static ArrayList<Variable> vars(String... names) {
        ArrayList<Variable> v = new ArrayList<Variable>();
        for (String n : names)
            v.add(new Variable(n));
        return v;
    }

    /**
     * Returns true if the given expression is a variable.
     *
     * @param e the expression
     * @return true if the given expression is a variable.
     */
    public static boolean isVar(Expression e) {
        return e instanceof Variable;
    }

    /**
     * Returns true if the given expression is a variable or a inverted variable.
     *
     * @param e the expression
     * @return true if the given expression is a variable or a inverted variable.
     */
    public static boolean isVarOrNotVar(Expression e) {
        return isVar(e) || (e instanceof Not && isVar(((Not) e).getExpression()));
    }

    /**
     * Creates a new intsnce
     *
     * @param identifier the variables name
     */
    public Variable(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean calculate(Context context) throws ExpressionException {
        return context.get(this);
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        v.visit(this);
        return v;
    }

    @Override
    public String getOrderString() {
        return identifier;
    }

    @Override
    public Expression copy() {
        return new Variable(identifier);
    }

    /**
     * @return the variables name
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variable that = (Variable) o;

        return identifier.equals(that.identifier);

    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public int compareTo(Variable o) {
        return identifier.compareTo(o.identifier);
    }

}
