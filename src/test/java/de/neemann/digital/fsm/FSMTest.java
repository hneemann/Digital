package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.VectorFloat;
import junit.framework.TestCase;

public class FSMTest extends TestCase {

    public void testSimple() throws FinitStateMachineException {
        FSM fsm = new FSM()
                .add(new State("0").setPosition(new VectorFloat(-1,0)))
                .add(new State("1").setPosition(new VectorFloat(0,1)))
                .add(new State("2").setPosition(new VectorFloat(1,0)))
                .add(new State("3").setPosition(new VectorFloat(0,-1)))
                .transition("0", "1", null)
                .transition("1", "2", null)
                .transition("2", "3", null)
                .transition("3", "0", null);

        fsm.calculateForces();
    }

}