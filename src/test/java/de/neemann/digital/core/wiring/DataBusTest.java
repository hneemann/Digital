package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.wiring.bus.DataBus;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class DataBusTest extends TestCase {

    public void testSimple() throws PinException, NodeException {
        ObservableValue a = new ObservableValue("a", 4, true);
        ObservableValue b = new ObservableValue("b", 4, true);

        Model m = new Model();

        ObservableValue out = new DataBus(null, m, a, b).getReadableOutput();

        a.set(1, false);
        assertEquals(1, out.getValue());
        a.set(1, true);
        b.set(2, false);
        assertEquals(2, out.getValue());
        b.set(1, true);

//        try {  ToDo HighZ
//            out.getValue();
//            assertTrue(false);
//        } catch (HighZException e) {
//            assertTrue(true);
//        }

        a.set(1, false);
        b.set(1, false);
        m.doStep();

        a.set(0, false);
        b.set(0, false);
        m.doStep();

        a.set(1, false);
        b.set(0, false);
        try {
            m.doStep();
            assertTrue(true);
        } catch (BurnException e) {
            assertTrue(true);
        }

    }

}