/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;

/**
 * Interface to optimize an expression
 */
public interface ExpressionOptimizer {

    /**
     * Optimizes the given expression.
     * Should call itself on the returned expression if a optimization was made.
     * See {@link ExprNot.OptimizeNotNot} as an example.
     *
     * @param expression the expression to optimize
     * @return the optimizes expression or the given expression if optimization is not possible
     */
    Expression optimize(Expression expression);
}
