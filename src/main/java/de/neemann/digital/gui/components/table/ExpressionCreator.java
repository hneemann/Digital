package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.TableRow;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelector;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;

import java.util.ArrayList;

/**
 * Used to generate the expressions belonging to the given truth table
 *
 * @author hneemann
 */
public class ExpressionCreator {

    private final TruthTable theTable;

    /**
     * Creates a new instance
     *
     * @param theTable the table to use
     */
    public ExpressionCreator(TruthTable theTable) {
        this.theTable = theTable;
    }

    /**
     * Creates the expressions
     *
     * @throws ExpressionException ExpressionException
     * @throws FormatterException  FormatterException
     */
    public void create(ExpressionListener listener) throws ExpressionException, FormatterException {
        ArrayList<Variable> vars = theTable.getVars();
        for (int table = 0; table < theTable.getResultCount(); table++) {
            PrimeSelector ps = new PrimeSelectorDefault();
            Expression e = new QuineMcCluskey(vars)
                    .fillTableWith(theTable.getResult(table))
                    .simplify(ps)
                    .getExpression();

            if (ps.getAllSolutions() != null) {
                for (ArrayList<TableRow> i : ps.getAllSolutions()) {
                    listener.resultFound(theTable.getResultName(table), QuineMcCluskey.addAnd(null, i, vars));
                }
            } else {
                listener.resultFound(theTable.getResultName(table), e);
            }
        }
    }


        /*
    private void resultFoundInt(String name, Expression expression) throws FormatterException, ExpressionException {
        resultFound(name, expression);

        if (name.endsWith("n+1") && createJK) {
            String detName = name.substring(0, name.length() - 2);
            DetermineJKStateMachine jk = new DetermineJKStateMachine(detName, expression);
            Expression j = jk.getJ();
            resultFound("J" + detName, j);
            Expression s = QuineMcCluskey.simplify(j);
            if (s != j) {
                resultFound("", s);
            }
            Expression k = jk.getK();
            resultFound("K" + detName, k);
            s = QuineMcCluskey.simplify(k);
            if (s != k) {
                resultFound("", s);
            }
        }
    }
    */
}
