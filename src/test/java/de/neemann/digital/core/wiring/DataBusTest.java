package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.HighZException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class DataBusTest extends TestCase {

    public void testSimple() throws PinException {
        ObservableValue a = new ObservableValue("a", 4, true);
        ObservableValue b = new ObservableValue("b", 4, true);

        ObservableValue out = new DataBus(null, a, b).getReadeableOutput();

        a.set(1, false);
        assertEquals(1, out.getValue());
        a.set(1, true);
        b.set(2, false);
        assertEquals(2, out.getValue());
        b.set(1, true);

        try {
            out.getValue();
            assertTrue(false);
        } catch (HighZException e) {
            assertTrue(true);
        }

        a.set(1, false);
        b.set(2, false);

        try {
            out.getValue();
            assertTrue(false);
        } catch (BurnException e) {
            assertTrue(true);
        }

    }

}