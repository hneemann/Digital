package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;

import java.util.ArrayList;

/**
 * Used to reorder the variables
 *
 * @author hneemann
 */
public class Reorder {

    private final TruthTable table;

    /**
     * Creates a new instance
     *
     * @param table the table to use
     */
    public Reorder(TruthTable table) {
        this.table = table;
    }

    /**
     * Reorders the variables
     *
     * @param swap the ordering
     * @return the new table
     * @throws ExpressionException ExpressionException
     */
    public TruthTable reorder(int[] swap) throws ExpressionException {
        checkSwapTable(swap);

        ArrayList<Variable> newVars = new ArrayList<>();
        for (int j = 0; j < table.getVars().size(); j++) {
            newVars.add(table.getVars().get(swap[j]));
        }

        TruthTable newTable = new TruthTable(newVars, table);

        ContextFiller fc = new ContextFiller(table.getVars());
        for (int row = 0; row < table.getRows(); row++) {
            fc.setContextTo(row);
            for (int t = 0; t < table.getResultCount(); t++)
                newTable.setByContext(t, fc, table.getByContext(t, fc));
        }

        return newTable;
    }

    private void checkSwapTable(int[] swap) {
        int cols = table.getVars().size();
        if (swap.length != cols)
            throw new RuntimeException("wrong swap list length!");

        for (int i = 0; i < cols; i++) {
            if (swap[i] < 0)
                throw new RuntimeException("swap index<0");
            if (swap[i] >= cols)
                throw new RuntimeException("swap index>" + (table.getCols() - 1));
            for (int j = 0; j < cols; j++) {
                if ((i != j) && swap[i] == swap[j])
                    throw new RuntimeException("two times the same swap index " + (swap[i]));
            }
        }
    }

}
