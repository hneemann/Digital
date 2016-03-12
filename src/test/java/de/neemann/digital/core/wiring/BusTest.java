package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class BusTest extends TestCase {

    public void testBus() throws Exception {
        Model model = new Model();
        ObservableValue a = new ObservableValue(1, true);
        ObservableValue b = new ObservableValue(1);
        ObservableValue out = model.add(new Bus(1)).addInput(a).addInput(b).getOutput();

        TestExecuter te = new TestExecuter(model).setInputs(a, b).setOutputs(out);
        te.check(0, 0, 0);
        te.check(0, 1, 1);
        a.setHighZ(false);
        b.setHighZ(true);
        te.check(0, 1, 0);
        te.check(1, 0, 1);
        a.setHighZ(false);
        b.setHighZ(false);
        try {
            te.check(0, 0, 0);
            assertTrue(false);
        } catch (BurnException e) {
            assertTrue(true);
        }
    }

    public void testBusHighZ() throws Exception {
        Model model = new Model();
        ObservableValue a = new ObservableValue(1, true);
        ObservableValue b = new ObservableValue(1, true);
        ObservableValue out = model.add(new Bus(1)).addInput(a).addInput(b).getOutput();

        TestExecuter te = new TestExecuter(model).setInputs(a, b).setOutputs(out);
        assertTrue(out.isHighZ());
    }
}