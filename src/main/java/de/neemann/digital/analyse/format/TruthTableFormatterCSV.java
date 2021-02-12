/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.core.Bits;

/**
 * Exports a table in LogicFriday format
 */
public class TruthTableFormatterCSV implements TruthTableFormatter {

    @Override
    public String format(TruthTable truthTable) throws ExpressionException {
        StringBuilder sb = new StringBuilder();
        for (String n : truthTable.getVarNames())
            sb.append(n).append(",");
        for (String n : truthTable.getResultNames())
            sb.append(',').append(n);
        sb.append('\n');

        export(sb, truthTable);

        return sb.toString();
    }

    private void export(StringBuilder sb, TruthTable truthTable) {
        int vars = truthTable.getVars().size();
        for (int r = 0; r < truthTable.getRows(); r++) {
            long m = Bits.up(1, vars - 1);
            for (int c = 0; c < vars; c++) {
                if ((r & m) == 0)
                    sb.append('0');
                else
                    sb.append('1');
                sb.append(',');
                m = m >> 1;
            }
            for (int c = 0; c < truthTable.getResultCount(); c++) {
                ThreeStateValue v = truthTable.getResult(c).get(r);
                sb.append(',').append(v.toString());
            }
            sb.append('\n');
        }
    }
}
