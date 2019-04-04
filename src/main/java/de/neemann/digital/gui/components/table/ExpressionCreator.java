/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.*;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.TableReducer;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Used to generate the expressions belonging to the given truth table
 */
public class ExpressionCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionCreator.class);
    private static final int MAX_INPUTS_ALLOWED = 12;
    private static final int COMPLEX_VAR_SIZE = 8;

    private final TruthTable theTable;
    private ProgressListener progressListener;

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
     * @param listener the listener to report the found expressions to
     * @throws ExpressionException ExpressionException
     * @throws FormatterException  FormatterException
     * @throws AnalyseException    AnalyseException
     */
    public void create(ExpressionListener listener) throws ExpressionException, FormatterException, AnalyseException {
        final List<Variable> vars = Collections.unmodifiableList(theTable.getVars());
        long time = System.currentTimeMillis();
        if (theTable.getResultCount() >= 4 && vars.size() > COMPLEX_VAR_SIZE) {
            LOGGER.debug("use parallel solvers");
            ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            ArrayList<Job> jobs = new ArrayList<>();
            for (int table = 0; table < theTable.getResultCount(); table++) {
                final ExpressionListenerStore l = new ExpressionListenerStore(null);
                jobs.add(simplify(l, vars, theTable.getResultName(table), theTable.getResult(table))
                        .setStorage(l));
            }

            LOGGER.debug("jobs: " + jobs.size());

            ArrayList<Job> orderedJobs = new ArrayList<>(jobs);
            orderedJobs.sort(Comparator.comparingInt(job -> -job.getComplexity()));

            for (Job j : orderedJobs) {
                ex.submit(() -> {
                    try {
                        j.run();
                        j.close();
                    } catch (ExpressionException | FormatterException e) {
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

            for (Job j : jobs)
                j.getStorage().replayTo(listener);

        } else {
            for (int table = 0; table < theTable.getResultCount(); table++)
                simplify(listener, vars, theTable.getResultName(table), theTable.getResult(table)).run();
        }
        listener.close();
        time = System.currentTimeMillis() - time;
        LOGGER.debug("time: " + time / 1000.0 + " sec");
        if (progressListener != null)
            progressListener.complete();
    }

    private Job simplify(ExpressionListener listener, List<Variable> vars, String resultName, BoolTable boolTable) throws AnalyseException, ExpressionException {
        List<Variable> localVars = vars;
        if (vars.size() > 4) {
            TableReducer tr = new TableReducer(vars, boolTable);
            if (tr.canReduce()) {
                LOGGER.debug(resultName + " reduced from " + vars.size() + " to " + tr.getVars().size() + " variables (" + tr.getVars() + ")");
                boolTable = tr.getTable();
                localVars = tr.getVars();
            }
        }
        if (!Main.isExperimentalMode() && localVars.size() > MAX_INPUTS_ALLOWED)
            throw new AnalyseException(Lang.get("err_toManyInputsIn_N0_max_N1_is_N2", resultName, MAX_INPUTS_ALLOWED, localVars.size()));


        listener = new CheckResultListener(listener, localVars, boolTable);

        return new Job(localVars, boolTable, resultName, listener);
    }

    private MinimizerInterface getMinimizer(int size) {
        if (size <= 4)
            return new MinimizerQuineMcCluskeyExam();
        else {
            return new MinimizerQuineMcCluskey();
        }
    }

    /**
     * Sets the progress listener to use
     *
     * @param progressListener the progress listener
     * @return this for chained calls
     */
    public ExpressionCreator setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * Listener used to monitor the progress
     */
    public interface ProgressListener {
        /**
         * Called if a equation is calculated
         */
        void oneCompleted();

        /**
         * Called if all equations are calculated
         */
        void complete();
    }

    private final class Job {
        private final List<Variable> localVars;
        private final BoolTable boolTable;
        private final String resultName;
        private final ExpressionListener listener;
        private ExpressionListenerStore storage;

        private Job(List<Variable> localVars, BoolTable boolTable, String resultName, ExpressionListener listener) {
            this.localVars = localVars;
            this.boolTable = boolTable;
            this.resultName = resultName;
            this.listener = listener;
        }

        private void run() throws ExpressionException, FormatterException {
            LOGGER.debug("start job with complexity " + getComplexity());
            long time = System.currentTimeMillis();
            getMinimizer(localVars.size()).minimize(localVars, boolTable, resultName, listener);
            LOGGER.debug("finished job with complexity " + getComplexity() + ":  " + (System.currentTimeMillis() - time) / 1000 + "sec");
            if (progressListener != null)
                progressListener.oneCompleted();
        }

        private int getComplexity() {
            return boolTable.realSize();
        }

        private void close() throws FormatterException, ExpressionException {
            listener.close();
        }

        private ExpressionListenerStore getStorage() {
            return storage;
        }

        private Job setStorage(ExpressionListenerStore storage) {
            this.storage = storage;
            return this;
        }
    }
}
