package de.neemann.digital.analyse.expression;

/**
 * @author hneemann
 */
public class ComplexityVisitor implements ExpressionVisitor {
    private int counter = 0;

    @Override
    public boolean visit(Expression expression) {
        if (!(expression instanceof Not))
            counter++;
        return true;
    }

    /**
     * Returns a measure for the complexity of the examined expression
     * The Not expression does not increase this complexity measure
     *
     * @return the complexity
     */
    public int getComplexity() {
        return counter;
    }
}
