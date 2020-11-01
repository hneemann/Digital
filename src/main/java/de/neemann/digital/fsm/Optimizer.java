/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.MinimizerInterface;
import de.neemann.digital.analyse.MinimizerQuineMcCluskey;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.ExpressionVisitor;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.core.Bits;
import de.neemann.digital.gui.components.table.ExpressionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Used to determine the optimal state numbers for a given FSM.
 */
public class Optimizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Optimizer.class);
    private static final long[] FAC_TABLE = new long[]{1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L};
    private final FSM fsm;

    private final int initialComplexity;
    private int bestComplexity;
    private int[] best;
    private Permute.PermPull pp;

    /**
     * Returns the factorial of a number.
     * If the result is greater as the max long value, the max value is returned.
     *
     * @param n integer
     * @return the factorial of the given number
     */
    public static long fac(int n) {
        if (n > 20)
            return Long.MAX_VALUE;
        return FAC_TABLE[n];
    }

    /**
     * Creates a new optimizer
     *
     * @param fsm the fsm to optimize
     * @throws FiniteStateMachineException FiniteStateMachineException
     * @throws FormatterException          FormatterException
     * @throws ExpressionException         ExpressionException
     */
    public Optimizer(FSM fsm) throws FiniteStateMachineException, FormatterException, ExpressionException {
        this.fsm = fsm;
        initialComplexity = calcComplexity(fsm, false);
        bestComplexity = initialComplexity;
    }

    /**
     * Called to optimize the state numbers in a FSM
     *
     * @return this for chained calls
     * @throws FiniteStateMachineException   FiniteStateMachineException
     * @throws FormatterException            FormatterException
     * @throws ExpressionException           ExpressionException
     * @throws Permute.PermListenerException PermListenerException
     */
    public Optimizer optimizeFSM() throws FiniteStateMachineException, FormatterException, ExpressionException, Permute.PermListenerException {
        LOGGER.info("optimizing time complexity: " + getTimeComplexity(fsm));

        bestComplexity = calcComplexity(fsm, false);
        LOGGER.info("start complexity " + bestComplexity);
        List<State> states = fsm.getStates();
        int size = states.size();
        int sizeInclDC = 1 << Bits.binLn2(size - 1);

        Permute.permute(size, sizeInclDC, perm -> {
            for (int i = 0; i < states.size(); i++)
                states.get(i).setNumber(perm[i]);

            int c;
            try {
                c = calcComplexity(fsm, false);
            } catch (ExpressionException | FiniteStateMachineException | FormatterException e) {
                throw new Permute.PermListenerException(e);
            }

            if (c < bestComplexity) {
                bestComplexity = c;
                best = Arrays.copyOf(perm, size);
            }
        });

        return this;
    }

    /**
     * Returns the time complexity of optimizing the given fsm
     *
     * @param fsm the fsm
     * @return the time complexity
     */
    public static long getTimeComplexity(FSM fsm) {
        List<State> states = fsm.getStates();
        int size = states.size();
        int sizeInclDC = 1 << Bits.binLn2(size - 1);

        if (sizeInclDC > 20)
            return Long.MAX_VALUE;

        return fac(sizeInclDC) / fac(sizeInclDC - size);
    }

    /**
     * Use to optimize the fsm by utilizing all evalable cores
     *
     * @param el the event listener to inform a client apout th state of the optimization
     * @return this for chained calls
     * @throws FiniteStateMachineException FiniteStateMachineException
     * @throws FormatterException          FormatterException
     * @throws ExpressionException         ExpressionException
     */
    public Optimizer optimizeFSMParallel(EventListener el) throws FiniteStateMachineException, FormatterException, ExpressionException {
        LOGGER.info("optimizing time complexity: " + getTimeComplexity(fsm));
        bestComplexity = calcComplexity(fsm, false);
        LOGGER.info("start complexity " + bestComplexity);
        List<State> states = fsm.getStates();
        int size = states.size();
        int sizeInclDC = 1 << Bits.binLn2(size - 1);
        pp = new Permute.PermPull(size, sizeInclDC);

        final Object lock = new Object();

        BestListener l = (b, bcplx) -> {
            synchronized (lock) {
                if (bcplx < bestComplexity) {
                    bestComplexity = bcplx;
                    best = b;
                    if (el != null)
                        el.bestSoFar(best, bestComplexity);
                }
            }
        };

        WaitGroup wg = new WaitGroup(() -> {
            if (el != null)
                el.finished();
        });
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            wg.add();
            new ThreadRunner(wg, new FSM(fsm), pp, l).start();
        }

        return this;
    }

    /**
     * Waits for the optimizer to finish
     *
     * @return this for chained calls
     */
    public Optimizer waitFor() {
        if (pp != null)
            pp.waitFor();

        return this;
    }

    /**
     * stops the optimizer
     */
    public void stop() {
        if (pp != null)
            pp.stop();
    }

    /**
     * Apply the best permutation to the fsm
     *
     * @return this for chained calls
     */
    public Optimizer applyBest() {
        if (best != null) {
            List<State> states = fsm.getStates();
            for (int i = 0; i < states.size(); i++)
                states.get(i).setNumber(best[i]);
        }
        return this;
    }

    /**
     * @return the minimal complexity
     */
    public int getBestComplexity() {
        return bestComplexity;
    }

    static int calcComplexity(FSM fsm, boolean out) throws
            ExpressionException, FiniteStateMachineException, FormatterException {
        TruthTable tt = fsm.createTruthTable(null);
        MinimizerInterface mi = new MinimizerQuineMcCluskey();
        ComplexityListener listener = new ComplexityListener(out);

        for (int i = 0; i < tt.getResultCount(); i++)
            mi.minimize(tt.getVars(), tt.getResult(i), tt.getResultName(i), listener);
        return listener.complexity;
    }

    /**
     * @return the initial complexity
     */
    public int getInitialComplexity() {
        return initialComplexity;
    }

    private final static class ComplexityListener implements ExpressionListener {
        private final boolean out;
        private int complexity;

        private ComplexityListener(boolean out) {
            this.out = out;
        }

        @Override
        public void resultFound(String name, Expression expression) {
            int complexity = expression.traverse(new ComplexityVisitorL()).getComplexity();

            if (out)
                System.out.println("   " + name + "=" + expression + "; " + complexity);

            this.complexity += complexity;
        }

        @Override
        public void close() {
        }

        private static class ComplexityVisitorL implements ExpressionVisitor {
            private int complexity;

            @Override
            public boolean visit(Expression expression) {
                if (expression instanceof Operation)
                    complexity += ((Operation) expression).getExpressions().size();
                return true;
            }

            public int getComplexity() {
                return complexity;
            }
        }
    }

    private static final class ThreadRunner extends Thread {
        private final WaitGroup wg;
        private final FSM fsm;
        private final Permute.PermPull pp;
        private final BestListener l;

        private ThreadRunner(WaitGroup wg, FSM fsm, Permute.PermPull pp, BestListener l) {
            this.wg = wg;
            this.fsm = fsm;
            this.pp = pp;
            this.l = l;
        }

        public void run() {
            try {
                int bestComplexity = Integer.MAX_VALUE;
                List<de.neemann.digital.fsm.State> states = fsm.getStates();
                int size = states.size();
                int[] p;
                while ((p = pp.next()) != null) {
                    for (int i = 0; i < size; i++)
                        states.get(i).setNumber(p[i]);

                    int c;
                    try {
                        c = calcComplexity(fsm, false);

                        if (c < bestComplexity) {
                            bestComplexity = c;
                            l.bestSoFar(Arrays.copyOf(p, size), bestComplexity);
                        }
                    } catch (ExpressionException | FiniteStateMachineException | FormatterException e) {
                        // do nothing
                    }
                }
            } finally {
                wg.done();
            }
        }
    }

    private interface BestListener {
        /**
         * Called if a new, better permutation is found
         *
         * @param best           the permutation
         * @param bestComplexity the complexity
         */
        void bestSoFar(int[] best, int bestComplexity);
    }

    /**
     * Used to inform the user of the parallel optimizer
     */
    public interface EventListener extends BestListener {
        /**
         * Called if the optimizer has finished
         */
        void finished();
    }

}
