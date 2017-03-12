package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.TableReducer;
import de.neemann.digital.analyse.quinemc.TableRow;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelector;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Used to generate the expressions belonging to the given truth table
 *
 * @author hneemann
 */
public class ExpressionCreator {

    private static final int MAX_INPUTS_ALLOWED = 12;

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
     * @throws AnalyseException    AnalyseException
     */
    public void create(ExpressionListener listener) throws ExpressionException, FormatterException, AnalyseException {
        final List<Variable> vars = Collections.unmodifiableList(theTable.getVars());
        long time = System.currentTimeMillis();
        if (theTable.getResultCount() > 100) {
            ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            ThreadSaveExpressionListener threadListener = new ThreadSaveExpressionListener(listener);
            for (int table = 0; table < theTable.getResultCount(); table++) {
                final int t = table;
                ex.submit(() -> {
                    try {
                        System.out.println("start " + t);
                        simplify(listener, vars, theTable.getResultName(t), theTable.getResult(t));
                        System.out.println("end " + t);
                    } catch (ExpressionException | FormatterException | AnalyseException e) {
                        e.printStackTrace();
                    }
                });
            }
            ex.shutdown();
            try {
                ex.awaitTermination(100, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadListener.close();
        } else {
            for (int table = 0; table < theTable.getResultCount(); table++)
                simplify(listener, vars, theTable.getResultName(table), theTable.getResult(table));
            listener.close();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("time: " + time / 1000.0 + " sec");
    }

    private void simplify(ExpressionListener listener, List<Variable> vars, String resultName, BoolTable boolTable) throws AnalyseException, ExpressionException, FormatterException {
        TableReducer tr = new TableReducer(vars, boolTable);
        List<Variable> localVars = vars;
        if (tr.canReduce()) {
            System.out.println(resultName + " reduced from " + vars.size() + " to " + tr.getVars().size() + " variables");
            boolTable = tr.getTable();
            localVars = tr.getVars();
        }
        if (localVars.size() > MAX_INPUTS_ALLOWED)
            throw new AnalyseException(Lang.get("err_toManyInputsIn_N0_max_N1_is_N2", resultName, MAX_INPUTS_ALLOWED, localVars.size()));

        QuineMcCluskey qmc = new QuineMcCluskey(localVars)
                .fillTableWith(boolTable);
        calcColumn(listener, qmc, resultName, localVars);
    }

    private static void calcColumn(ExpressionListener listener, QuineMcCluskey qmc, String name, List<Variable> vars) throws ExpressionException, FormatterException {
        PrimeSelector ps = new PrimeSelectorDefault();
        Expression e = qmc.simplify(ps).getExpression();

        if (ps.getAllSolutions() != null) {
            for (ArrayList<TableRow> i : ps.getAllSolutions()) {
                listener.resultFound(name, QuineMcCluskey.addAnd(null, i, vars));
            }
        } else {
            listener.resultFound(name, e);
        }
    }

    private final static class ThreadSaveExpressionListener implements ExpressionListener {
        private final ExpressionListener listener;

        private ThreadSaveExpressionListener(ExpressionListener listener) {
            this.listener = listener;
        }

        @Override
        public synchronized void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
            listener.resultFound(name, expression);
        }

        @Override
        public synchronized void close() throws FormatterException, ExpressionException {
            listener.close();
        }
    }
}
