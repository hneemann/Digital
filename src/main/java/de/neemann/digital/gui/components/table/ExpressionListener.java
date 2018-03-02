/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;

/**
 * Interface used by the {@link ExpressionCreator} to deliver the found expressions.
 */
public interface ExpressionListener {
    /**
     * Method to overload to handle all found solutions
     *
     * @param name       the results name
     * @param expression the calculated expressdion
     * @throws FormatterException  FormatterException
     * @throws ExpressionException ExpressionException
     */
    void resultFound(String name, Expression expression) throws FormatterException, ExpressionException;

    /**
     * Called if last expression was created
     *
     * @throws FormatterException  FormatterException
     * @throws ExpressionException ExpressionException
     */
    void close() throws FormatterException, ExpressionException;
}
