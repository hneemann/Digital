/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.format.FormatToExpression;

/**
 * Formats expressions to plain text
 */
public class PlainTextExpressionListener implements ExpressionListener {
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void resultFound(String name, Expression expression) {
        String exp = FormatToExpression.defaultFormat(expression);
        sb.append(name);
        sb.append(" = ");
        sb.append(exp);
        sb.append('\n');
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
