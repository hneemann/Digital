/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.NamedExpression;
import de.neemann.digital.analyse.format.TruthTableFormatterLaTeX;
import de.neemann.digital.draw.graphics.text.formatter.LaTeXFormatter;

final class LaTeXExpressionListener implements ExpressionListener {
    private final StringBuilder sb;

    LaTeXExpressionListener(TruthTable truthTable) throws ExpressionException {
        sb = new StringBuilder();
        if (truthTable.getRows() <= 256) {
            String text = new TruthTableFormatterLaTeX().format(truthTable);
            sb.append(text);
        }
        sb.append("\\begin{eqnarray*}\n");
    }

    @Override
    public void resultFound(String name, Expression expression) {
        sb.append(LaTeXFormatter.format(new NamedExpression(name, expression)))
                .append("\\\\\n");
    }

    @Override
    public void close() {
        sb.append("\\end{eqnarray*}\n");
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
