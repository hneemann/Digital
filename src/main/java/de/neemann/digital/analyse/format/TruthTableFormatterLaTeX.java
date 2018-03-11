/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToTableLatex;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;

/**
 */
public class TruthTableFormatterLaTeX implements TruthTableFormatter {
    @Override
    public String format(TruthTable truthTable) throws ExpressionException {
        StringBuilder sb = new StringBuilder();
        sb.append("\\begin{center}\n\\begin{tabular}{");
        for (Variable v : truthTable.getVars())
            sb.append("c");
        sb.append("|");
        for (int i = 0; i < truthTable.getResultCount(); i++)
            sb.append("c");
        sb.append("}\n");

        for (Variable v : truthTable.getVars())
            sb.append("$").append(formatVar(v.getIdentifier())).append("$&");
        for (int i = 0; i < truthTable.getResultCount(); i++) {
            sb.append("$").append(formatVar(truthTable.getResultName(i))).append("$");
            if (i < truthTable.getResultCount() - 1)
                sb.append("&");
        }
        sb.append("\\\\\n");
        sb.append("\\hline\n");

        ContextFiller cf = new ContextFiller(truthTable.getVars());
        for (int i = 0; i < cf.getRowCount(); i++) {
            cf.setContextTo(i);
            for (Variable v : cf)
                sb.append(format(cf.get(v))).append("&");

            for (int j = 0; j < truthTable.getResultCount(); j++) {
                ThreeStateValue r = truthTable.getResult(j).get(i);
                sb.append(format(r));
                if (j < truthTable.getResultCount() - 1)
                    sb.append("&");
            }
            sb.append("\\\\\n");
        }
        sb.append("\\end{tabular}\n\\end{center}\n");
        return sb.toString();
    }

    private String format(boolean b) {
        return format(ThreeStateValue.value(b));
    }

    private String format(ThreeStateValue r) {
        switch (r) {
            case one:
                return "$1$";
            case zero:
                return "$0$";
            case dontCare:
                return "-";
        }
        return null;
    }

    private static String formatVar(String var) {
        return FormatToTableLatex.formatIdentifier(var);
    }
}
