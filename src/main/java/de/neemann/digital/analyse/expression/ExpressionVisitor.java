package de.neemann.digital.analyse.expression;

/**
 * Visitor used the visit all sub expressions of the expression tree
 *
 * @author hneemann
 */
public interface ExpressionVisitor {

    /**
     * if true is returned the visitor goes down the tree.
     *
     * @param expression the expression to visit
     * @return if true operation goes down
     */
    boolean visit(Expression expression);
}
