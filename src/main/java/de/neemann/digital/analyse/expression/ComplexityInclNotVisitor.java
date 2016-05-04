package de.neemann.digital.analyse.expression;

/**
 * @author hneemann
 */
public class ComplexityInclNotVisitor implements ExpressionVisitor {
    private int counter = 0;

    @Override
    public boolean visit(Expression expression) {
        counter++;
        return true;
    }

    /**
     * Returns a measure for the complexity of the examined expression
     *
     * @return the complexity
     */
    public int getComplexity() {
        return counter;
    }
}
