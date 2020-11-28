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
import java.util.List;

import static de.neemann.digital.fsm.Optimizer.fac;

public class OptimizerTest extends TestCase {

    public void testStepper() throws IOException, FiniteStateMachineException, FormatterException, ExpressionException, Permute.PermListenerException {
        FSM fsm = FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/stepperSimple.fsm"));
        Optimizer optimizer = new Optimizer(fsm).optimizeFSM().applyBest();
        assertEquals(12, optimizer.getBestComplexity());
        List<State> states = fsm.getStates();
        checkState(states.get(0), "S0", 0);
        checkState(states.get(1), "S1", 1);
        checkState(states.get(2), "S2", 3);
        checkState(states.get(3), "S3", 2);
    }

    private void checkState(State state, String name, int num) {
        assertEquals(name, state.getName());
        assertEquals(num, state.getNumber());
    }

    public void testTrafficLight() throws IOException, FiniteStateMachineException, FormatterException, ExpressionException, Permute.PermListenerException {
        FSM fsm = FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/trafficLightBlink.fsm"));
        Optimizer optimizer = new Optimizer(fsm).optimizeFSM();
        assertEquals(9, optimizer.getBestComplexity());
    }

    public void testTimeComplexity() throws IOException {
        assertEquals(4 * 3 * 2, Optimizer.getTimeComplexity(FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/stepperSimple.fsm"))));
        assertEquals(8 * 7 * 6 * 5 * 4, Optimizer.getTimeComplexity(FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/trafficLightBlink.fsm"))));
    }

    public void testParallel() throws IOException, ExpressionException, FiniteStateMachineException, FormatterException, Permute.PermListenerException {
        FSM fsm = FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/trafficLightBlink.fsm"));
//        FSM fsm = FSM.loadFSM(new File(Resources.getRoot(), "../../main/fsm/SevenSegCounter.fsm"));
        new Optimizer(fsm).optimizeFSMParallel(null).waitFor().applyBest();
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