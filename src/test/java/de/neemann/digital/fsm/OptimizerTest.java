/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static de.neemann.digital.fsm.Optimizer.fac;
import static de.neemann.digital.fsm.Optimizer.permute;

public class OptimizerTest extends TestCase {

    public void testPermuteSimple() throws FiniteStateMachineException, FormatterException, ExpressionException {
        for (int i = 3; i <= 7; i++) {
            TestListener testlistener = new TestListener();
            permute(i, testlistener);
            assertEquals("i=" + i, fac(i), testlistener.set.size());
        }
    }

    public void testPermuteRange() throws FiniteStateMachineException, FormatterException, ExpressionException {
        for (int r = 1; r <= 3; r++)
            for (int i = 3; i <= 6; i++) {
                TestListener testlistener = new TestListener();
                permute(i, i + r, testlistener);
                assertEquals("i=" + i + ", r=" + r, fac(i + r) / fac(r), testlistener.set.size());
            }
    }

    public void testStepper() throws IOException, FiniteStateMachineException, FormatterException, ExpressionException, Optimizer.OptimizerException {
        FSM fsm = FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/stepperSimple.fsm"));
        Optimizer optimizer = new Optimizer();
        optimizer.optimizeFSM(fsm);
        assertEquals(12, optimizer.getBestComplexity());
        List<State> states = fsm.getStates();
        checkState(states.get(0), "S0", 0);
        checkState(states.get(1), "S1", 1);
        checkState(states.get(2), "S2", 3);
        checkState(states.get(3), "S3", 2);
    }

    public void testTrafficLight() throws IOException, FiniteStateMachineException, FormatterException, ExpressionException, Optimizer.OptimizerException {
        FSM fsm = FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/trafficLightBlink.fsm"));
        Optimizer optimizer = new Optimizer();
        optimizer.optimizeFSM(fsm);
        assertEquals(9, optimizer.getBestComplexity());
    }

    public void testTimeComplexity() throws IOException {
        assertEquals(4 * 3 * 2, Optimizer.getTimeComplexity(FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/stepperSimple.fsm"))));
        assertEquals(8 * 7 * 6 * 5 * 4, Optimizer.getTimeComplexity(FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/trafficLightBlink.fsm"))));
    }


    private void checkState(State state, String name, int num) {
        assertEquals(name, state.getName());
        assertEquals(num, state.getNumber());
    }

    private static class TestListener implements Optimizer.PermListener {
        private final TreeSet<Perm> set = new TreeSet<>();

        @Override
        public void perm(int[] perm) {
            Perm p = new Perm(perm);
            assertFalse(set.contains(p));
            set.add(p);
        }
    }

    private static class Perm implements Comparable<Perm> {
        private final int[] perm;

        public Perm(int[] perm) {
            this.perm = Arrays.copyOf(perm, perm.length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Perm perm1 = (Perm) o;
            return Arrays.equals(perm, perm1.perm);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(perm);
        }

        @Override
        public String toString() {
            return "Perm{" +
                    "perm=" + Arrays.toString(perm) +
                    '}';
        }

        @Override
        public int compareTo(Perm other) {
            for (int i = 0; i < perm.length; i++) {
                int r = Integer.compare(this.perm[i], other.perm[i]);
                if (r != 0)
                    return r;
            }
            return 0;
        }
    }

    public void testFac() {
        assertEquals(1, fac(1));
        assertEquals(2, fac(2));
        assertEquals(6, fac(3));
        assertEquals(24, fac(4));
        assertEquals(120, fac(5));
        assertEquals(720, fac(6));
        assertEquals(5040, fac(7));

        assertEquals(2432902008176640000L, fac(20));
        assertEquals(Long.MAX_VALUE, fac(21));
    }
}