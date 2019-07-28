/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;


import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.draw.graphics.text.formatter.LaTeXFormatter;

/**
 */
public class FormatToTableLatex extends FormatToTable {

    @Override
    protected void formatHead(StringBuilder sb, int varCount) {
        sb.append("\\begin{tabular}{");
        for (int i = 0; i < varCount; i++)
            sb.append("c");
        sb.append("|c}\n");
    }

    @Override
    protected String formatVariable(Variable v) {
        return "$" + LaTeXFormatter.format(v) + "$&";
    }

    @Override
    protected String formatResultVariable() {
        return "$Y$\\\\";
    }

    @Override
    protected void formatTableStart(StringBuilder sb) {
        sb.append("\\hline\n");
    }

    @Override
    protected String formatValue(boolean val) {
        return super.formatValue(val) + "&";
    }

    @Override
    protected String formatResult(boolean value) {
        return super.formatValue(value) + "\\\\";
    }

    @Override
    protected void formatEnd(StringBuilder sb) {
        sb.append("\\end{tabular}\n");
    }

}
