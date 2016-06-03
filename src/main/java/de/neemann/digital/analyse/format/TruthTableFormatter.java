package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * Formats a truth table
 *
 * @author hneemann
 */
public interface TruthTableFormatter {

    /**
     * Creates a string representation of the table
     *
     * @param truthTable the table
     * @return the string representation
     * @throws ExpressionException ExpressionException
     */
    String format(TruthTable truthTable) throws ExpressionException;
}
