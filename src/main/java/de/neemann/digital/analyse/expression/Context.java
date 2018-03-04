/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

/**
 * The context used to evaluate an expression
 */
public interface Context {
    /**
     * Returns the value of the given variable
     *
     * @param variable the variable
     * @return the variables value
     * @throws ExpressionException ExpressionException
     */
    boolean get(Variable variable) throws ExpressionException;
}
