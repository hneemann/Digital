/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.MinimizerQuineMcCluskey;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import junit.framework.TestCase;

public class TransitionTableCreatorTest extends TestCase {

    public void testBlink() throws ExpressionException, FiniteStateMachineException, FormatterException {
        State a = new State("a");
        State b = new State("b");
        FSM fsm = new FSM(a, b)
                .transition(a, b, null)
                .transition(b, a, null);
        TruthTable tt = new TransitionTableCreator(fsm).create();
        assertEquals(2, tt.getRows());
        assertEquals(1, tt.getResultCount());

        final ExpressionListenerStore el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(0), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("not(Z_0^n)", el.getFirst().toString());
    }

    public void testBlinkOnOff() throws ExpressionException, FiniteStateMachineException, FormatterException {
        State a = new State("a");
        State b = new State("b");
        FSM fsm = new FSM(a, b)
                .transition(a, b, "Run")
                .transition(b, a, "Run");
        TruthTable tt = new TransitionTableCreator(fsm).create();
        assertEquals(4, tt.getRows());
        assertEquals(1, tt.getResultCount());

        final ExpressionListenerStore el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(0), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("or(and(Run,not(Z_0^n)),and(not(Run),Z_0^n))", el.getFirst().toString());
    }

    public void testBlinkResult() throws ExpressionException, FiniteStateMachineException, FormatterException {
        State a = new State("a").setValues("y=0");
        State b = new State("b").setValues("y=1");
        FSM fsm = new FSM(a, b)
                .transition(a, b, null)
                .transition(b, a, null);
        TruthTable tt = new TransitionTableCreator(fsm).create();
        assertEquals(2, tt.getRows());
        assertEquals(2, tt.getResultCount());

        ExpressionListenerStore el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(0), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("not(Z_0^n)", el.getFirst().toString());

        el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(1), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("Z_0^n", el.getFirst().toString());
    }

    public void testBlinkNotDeterministic() throws ExpressionException {
        State a = new State("a");
        State b = new State("b");
        State c = new State("c");
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, "Run")
                .transition(a, c, "Run");
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FiniteStateMachineException e) {
            assertTrue(true);
        }
    }

    public void testBlinkNotDeterministicDef() throws ExpressionException {
        State a = new State("a");
        State b = new State("b");
        State c = new State("c");
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, null)
                .transition(a, c, null);
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FiniteStateMachineException e) {
            assertTrue(true);
        }
    }

    public void testBlinkNotUnique() throws ExpressionException {
        State a = new State("a");
        State b = new State("b").setNumber(1);
        State c = new State("c").setNumber(1);
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, "Run")
                .transition(b, c, "Run");
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FiniteStateMachineException e) {
            assertTrue(true);
        }
    }

    public void testBlinkNoInitialState() throws ExpressionException {
        State a = new State("a").setNumber(1);
        State b = new State("b").setNumber(2);
        State c = new State("c").setNumber(3);
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, "Run")
                .transition(b, c, "Run");

        a.setInitial(false);
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FiniteStateMachineException e) {
            assertTrue(true);
        }
    }

    public void testMealy() throws Exception {
        State a = new State("a").setNumber(0);
        State b = new State("b").setNumber(1);
        FSM fsm = new FSM(a, b)
                .add(new Transition(a,b,"R").setValues("Y=1"))
                .transition(b, a, "");

        TruthTable tt = new TransitionTableCreator(fsm).create();
        assertEquals(4, tt.getRows());
        assertEquals(2, tt.getResultCount());

        ExpressionListenerStore el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(0), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("and(R,not(Z_0^n))", el.getFirst().toString());

        el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(1), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("and(R,not(Z_0^n))", el.getFirst().toString());

    }

}