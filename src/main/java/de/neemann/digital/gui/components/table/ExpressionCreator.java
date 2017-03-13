package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.MinimizerInterface;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.TableReducer;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionCreator.class);
    private static final int MAX_INPUTS_ALLOWED = 12;

    private final TruthTable theTable;
    private final MinimizerInterface minimizer;

    /**
     * Creates a new instance
     *
     * @param theTable  the table to use
     * @param minimizer the minimizer to use
     */
    public ExpressionCreator(TruthTable theTable, MinimizerInterface minimizer) {
        this.theTable = theTable;
        this.minimizer = minimizer;
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
                        simplify(listener, vars, theTable.getResultName(t), theTable.getResult(t));
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
        LOGGER.debug("time: " + time / 1000.0 + " sec");
    }

    private void simplify(ExpressionListener listener, List<Variable> vars, String resultName, BoolTable boolTable) throws AnalyseException, ExpressionException, FormatterException {
        TableReducer tr = new TableReducer(vars, boolTable);
        List<Variable> localVars = vars;
        if (tr.canReduce()) {
            LOGGER.debug(resultName + " reduced from " + vars.size() + " to " + tr.getVars().size() + " variables");
            boolTable = tr.getTable();
            localVars = tr.getVars();
        }
        if (!Main.enableExperimental() && localVars.size() > MAX_INPUTS_ALLOWED)
            throw new AnalyseException(Lang.get("err_toManyInputsIn_N0_max_N1_is_N2", resultName, MAX_INPUTS_ALLOWED, localVars.size()));

        minimizer.minimize(localVars, boolTable, resultName, listener);
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
