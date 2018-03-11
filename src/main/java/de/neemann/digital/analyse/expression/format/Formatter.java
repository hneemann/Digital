/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;


import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * Used to format an expression
 */
public interface Formatter {

    /**
     * Formats an expression
     *
     * @param expression the expression
     * @return the formatted expression
     * @throws FormatterException  FormatterException
     * @throws ExpressionException ExpressionException
     */
    String format(Expression expression) throws FormatterException, ExpressionException;
}
