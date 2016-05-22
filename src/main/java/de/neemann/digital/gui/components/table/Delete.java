package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;

import java.util.ArrayList;

/**
 * Used to delete a column from a {@link TruthTable}
 *
 * @author hneemann
 */
public class Delete {

    private final TruthTable table;

    /**
     * Creates a new instance
     *
     * @param table the table to use
     */
    public Delete(TruthTable table) {
        this.table = table;
    }

    /**
     * Deletes a column
     *
     * @param i the column to delete
     * @return the new table
     * @throws ExpressionException ExpressionException
     */
    public TruthTable delete(int i) throws ExpressionException {

        int vars = table.getVars().size();
        if (i >= vars) {
            if (table.getResultCount() > 1)
                table.removeResult(i - vars);
            return table;
        } else {
            if (table.getVars().size() < 3)
                return table;

            ArrayList<Variable> newVars = new ArrayList<>();
            for (int j = 0; j < table.getVars().size(); j++) {
                if (j != i)
                    newVars.add(table.getVars().get(j));
            }

            TruthTable newTable = new TruthTable(newVars);
            for (int j = 0; j < table.getResultCount(); j++)
                newTable.addResult(table.getResultName(j));

            ContextFiller fc = new ContextFiller(newTable.getVars());
            fc.set(table.getVars().get(i), false);
            for (int row = 0; row < newTable.getRows(); row++) {
                fc.setContextTo(row);
                for (int t = 0; t < newTable.getResultCount(); t++)
                    newTable.setByContext(t, fc, table.getByContext(t, fc));
            }

            return newTable;
        }
    }

}
