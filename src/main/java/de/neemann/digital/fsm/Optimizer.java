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
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.table.ExpressionListener;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Used to determine the optimal state numbers for a given FSM.
 */
public class Optimizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Optimizer.class);

    private int bestComplexity = Integer.MAX_VALUE;
    private int[] best;

    static void permute(int size, PermListener listener) throws FiniteStateMachineException, FormatterException, ExpressionException {
        permute(size, size, listener);
    }

    static void permute(int size, int range, PermListener listener) throws FiniteStateMachineException, FormatterException, ExpressionException {
        int[] perms = new int[range];
        for (int i = 0; i < range; i++)
            perms[i] = i;
        permute(perms, 0, size, listener);
    }

    private static void permute(int[] perms, int fixed, int size, PermListener listener) throws FiniteStateMachineException, FormatterException, ExpressionException {
        if (fixed == size) {
            listener.perm(perms);
            return;
        }

        permute(perms, fixed + 1, size, listener);
        for (int i = fixed + 1; i < perms.length; i++) {
            swap(perms, fixed, i);
            permute(perms, fixed + 1, size, listener);
            swap(perms, fixed, i);
        }
    }

    private static void swap(int[] perms, int n0, int n1) {
        int t = perms[n0];
        perms[n0] = perms[n1];
        perms[n1] = t;
    }

    private static final long[] FAC_TABLE = new long[]{1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L};

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
     * Called to optimize the state numbers in a FSM
     *
     * @param fsm the fsm to optimize
     * @throws FiniteStateMachineException FiniteStateMachineException
     * @throws FormatterException          FormatterException
     * @throws ExpressionException         ExpressionException
     * @throws OptimizerException          OptimizerException
     */
    public void optimizeFSM(FSM fsm) throws FiniteStateMachineException, FormatterException, ExpressionException, OptimizerException {
        LOGGER.info("optimizing time complexity: " + getTimeComplexity(fsm));

        final long timeOut = Main.isExperimentalMode() ? Long.MAX_VALUE : System.currentTimeMillis() + 1000L * 30;

        bestComplexity = calcComplexity(fsm, false);
        LOGGER.info("start complexity " + bestComplexity);

        ArrayBlockingQueue<int[]> queue = new ArrayBlockingQueue<>(50);

        List<State> states = fsm.getStates();
        int size = states.size();
        int sizeInclDC = 1 << Bits.binLn2(size - 1);
        try {
            permute(states.size(), sizeInclDC, perm -> {
                for (int i = 0; i < states.size(); i++)
                    states.get(i).setNumber(perm[i]);

                int c = calcComplexity(fsm, false);

                if (c < bestComplexity) {
                    bestComplexity = c;
                    best = Arrays.copyOf(perm, size);
                    bestSoFar(fsm, bestComplexity);
                }

                if (System.currentTimeMillis() > timeOut)
                    throw new TimeoutException();

            });
        } catch (TimeoutException e) {
            throw new OptimizerException(Lang.get("err_fsm_optimizer_timeout"));
        } finally {
            if (best != null)
                for (int i = 0; i < size; i++)
                    states.get(i).setNumber(best[i]);
        }
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
     * Called if a new optimal fsm is found
     *
     * @param fsm        the fsm
     * @param complexity the complexity of the fsm
     */
    public void bestSoFar(FSM fsm, int complexity) {
        LOGGER.info(fsm.getStates() + "; " + complexity);
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

    private static final class TimeoutException extends RuntimeException {
    }

    /**
     * Exception thrown by the optimizer
     */
    public static final class OptimizerException extends Exception {
        private OptimizerException(String message) {
            super(message);
        }
    }

    interface PermListener {
        void perm(int[] perm) throws FiniteStateMachineException, FormatterException, ExpressionException;
    }

}
