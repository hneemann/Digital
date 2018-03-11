/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

/**
 * Visitor used the visit all sub expressions of the expression tree
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
