package de.neemann.digital.analyse.expression;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class Variable implements Comparable<Variable>, Expression {

    private String identifier;

    public static Variable v(String name) {
        return new Variable(name);
    }

    public static ArrayList<Variable> vars(int n) {
        ArrayList<Variable> v = new ArrayList<Variable>();
        for (int i = 0; i < n; i++)
            v.add(new Variable("" + (char) ('A' + i)));
        return v;
    }

    public static ArrayList<Variable> vars(String... names) {
        ArrayList<Variable> v = new ArrayList<Variable>();
        for (String n : names)
            v.add(new Variable(n));
        return v;
    }

    public Variable(String identifier) {
        this.identifier = identifier;
    }

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

    public String getIdentifier() {
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
