/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;

/**
 * Visitor to visit all expressions
 */
public interface Visitor {
    /**
     * Visited by all expressions
     *
     * @param expression the expression
     */
    void visit(Expression expression);
}
