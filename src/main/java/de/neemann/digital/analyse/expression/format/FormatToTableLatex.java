/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;


import de.neemann.digital.analyse.expression.Variable;

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
        return "$" + formatIdentifier(super.formatVariable(v)) + "$&";
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

    /**
     * Formats the given identifier
     *
     * @param identifier the identifier
     * @return the formatted text
     */
    public static String formatIdentifier(String identifier) {
        int p = identifier.indexOf("_");
        if (p < 0) return identifier;

        String index = identifier.substring(p + 1);
        return identifier.substring(0, p + 1) + "{" + index + "}";
    }
}
