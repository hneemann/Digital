package de.neemann.digital.fsm;

import de.neemann.digital.analyse.MinimizerQuineMcCluskey;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Variable.v;

public class TransitionTableCreatorTest extends TestCase {

    public void testBlink() throws ExpressionException, FinitStateMachineException, FormatterException {
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
        assertEquals("not(Q0_n)", el.getFirst().toString());
    }

    public void testBlinkOnOff() throws ExpressionException, FinitStateMachineException, FormatterException {
        State a = new State("a");
        State b = new State("b");
        FSM fsm = new FSM(a, b)
                .transition(a, b, v("Run"))
                .transition(b, a, v("Run"));
        TruthTable tt = new TransitionTableCreator(fsm).create();
        assertEquals(4, tt.getRows());
        assertEquals(1, tt.getResultCount());

        final ExpressionListenerStore el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(0), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("or(and(not(Q0_n),Run),and(Q0_n,not(Run)))", el.getFirst().toString());
    }

    public void testBlinkResult() throws ExpressionException, FinitStateMachineException, FormatterException {
        State a = new State("a").val("y",0);
        State b = new State("b").val("y",1);
        FSM fsm = new FSM(a, b)
                .transition(a, b, null)
                .transition(b, a, null);
        TruthTable tt = new TransitionTableCreator(fsm).create();
        assertEquals(2, tt.getRows());
        assertEquals(2, tt.getResultCount());

        ExpressionListenerStore el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(0), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("not(Q0_n)", el.getFirst().toString());

        el = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskey().minimize(tt.getVars(), tt.getResult(1), "Y", el);

        assertEquals(1, el.getResults().size());
        assertEquals("Q0_n", el.getFirst().toString());
    }

    public void testBlinkNotDeterministic() throws ExpressionException {
        State a = new State("a");
        State b = new State("b");
        State c = new State("c");
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, v("Run"))
                .transition(a, c, v("Run"));
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FinitStateMachineException e) {
            assertTrue(true);
        }
    }

    public void testBlinkNotUnique() throws ExpressionException {
        State a = new State("a");
        State b = new State("b").setNumber(1);
        State c = new State("c").setNumber(1);
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, v("Run"))
                .transition(b, c, v("Run"));
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FinitStateMachineException e) {
            assertTrue(true);
        }
    }

    public void testBlinkNoInitialState() throws ExpressionException {
        State a = new State("a").setNumber(1);
        State b = new State("b").setNumber(2);
        State c = new State("c").setNumber(3);
        FSM fsm = new FSM(a, b, c)
                .transition(a, b, v("Run"))
                .transition(b, c, v("Run"));
        try {
            new TransitionTableCreator(fsm).create();
            fail();
        } catch (FinitStateMachineException e) {
            assertTrue(true);
        }
    }

}