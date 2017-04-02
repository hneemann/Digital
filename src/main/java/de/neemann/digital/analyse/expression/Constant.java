package de.neemann.digital.analyse.expression;

/**
 * A constant
 *
 * @author hneemann
 */
public final class Constant implements Expression {
    private final boolean value;

    /**
     * The constant true or one
     */
    public static final Constant ONE = new Constant(true);

    /**
     * The constant false or zero
     */
    public static final Constant ZERO = new Constant(false);

    private Constant(boolean value) {
        this.value = value;
    }

    @Override
    public boolean calculate(Context context) {
        return value;
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        v.visit(this);
        return v;
    }

    @Override
    public String getOrderString() {
        return Boolean.toString(value);
    }

    /**
     * Retorns the constants value
     *
     * @return the value
     */
    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public Expression copy() {
        return new Constant(value);
    }
}
