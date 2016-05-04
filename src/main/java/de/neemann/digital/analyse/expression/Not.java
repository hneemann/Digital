package de.neemann.digital.analyse.expression;

/**
 * @author hneemann
 */
public final class Not implements Expression {

    private final Expression expression;

    /**
     * Creates a not expression
     *
     * @param a the child expression to invert
     * @return the inverted expression
     */
    public static Expression not(Expression a) {
        if (a == Constant.ONE)
            return Constant.ZERO;
        if (a == Constant.ZERO)
            return Constant.ONE;

        if (a instanceof Not) {
            return ((Not) a).expression;
        } else
            return new Not(a);
    }

    private Not(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean calculate(Context context) throws ExpressionException {
        return !expression.calculate(context);
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V v) {
        if (v.visit(this)) {
            expression.traverse(v);
        }
        return v;
    }

    @Override
    public String getOrderString() {
        return expression.getOrderString();
    }

    /**
     * @return the negated expression
     */
    public Expression getExpression() {
        return expression;
    }
}
