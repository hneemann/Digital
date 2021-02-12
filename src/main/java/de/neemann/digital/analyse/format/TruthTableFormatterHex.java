/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.lang.Lang;

/**
 * Formats a truth table
 */
public class TruthTableFormatterHex implements TruthTableFormatter {

    @Override
    public String format(TruthTable truthTable) throws ExpressionException {
        if (truthTable.getResultCount() > 63)
            throw new ExpressionException(Lang.get("err_tableHasToManyResultColumns"));

        StringBuilder sb = new StringBuilder();
        sb.append("v2.0 raw\n");

        int count = truthTable.getResult(0).size();
        for (int i = 0; i < count; i++) {
            long val = 0;
            long mask = 1;
            for (int j = 0; j < truthTable.getResultCount(); j++) {
                ThreeStateValue v = truthTable.getResult(j).get(i);
                if (v == ThreeStateValue.one)
                    val |= mask;
                mask *= 2;
            }
            sb.append(Long.toHexString(val));
            sb.append('\n');
        }
        return sb.toString();
    }
}
