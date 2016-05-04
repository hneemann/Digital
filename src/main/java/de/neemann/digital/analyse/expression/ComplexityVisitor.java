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

    public int getComplexity() {
        return counter;
    }
}
