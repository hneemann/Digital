package de.neemann.digital.core.wiring;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

/**
 * Created by hneemann on 12.11.16.
 */
public class SwitchTest extends TestCase {

    public void testSwitchOn() throws NodeException {
        ObservableValue in = new ObservableValue("a", 1);

        Switch aSwitch = new Switch(new ElementAttributes().set(Keys.CLOSED, true));
        aSwitch.setInputs(in.asList());
        ObservableValue out = aSwitch.getOutputs().get(0);

        in.set(1,false);
        assertEquals(1, out.getValue());
        assertFalse(out.isHighZ());

        in.set(0,false);
        assertEquals(0, out.getValue());
        assertFalse(out.isHighZ());

        in.set(0,true);
        assertTrue(out.isHighZ());
    }

    public void testSwitchOff() throws NodeException {
        ObservableValue in = new ObservableValue("a", 1);

        Switch aSwitch = new Switch(new ElementAttributes().set(Keys.CLOSED, false));
        aSwitch.setInputs(in.asList());
        ObservableValue out = aSwitch.getOutputs().get(0);

        in.set(1,false);
        assertTrue(out.isHighZ());

        in.set(0,false);
        assertTrue(out.isHighZ());

        in.set(0,true);
        assertTrue(out.isHighZ());
    }

    public void testSwitchOn4Bits() throws NodeException {
        ObservableValue in = new ObservableValue("a", 4);

        Switch aSwitch = new Switch(new ElementAttributes().set(Keys.CLOSED, true).setBits(4));
        aSwitch.setInputs(in.asList());
        ObservableValue out = aSwitch.getOutputs().get(0);
        assertEquals(4, out.getBits());

        in.set(5,false);
        assertEquals(5, out.getValue());
        assertFalse(out.isHighZ());

        in.set(10,false);
        assertEquals(10, out.getValue());
        assertFalse(out.isHighZ());

        in.set(0,true);
        assertTrue(out.isHighZ());
    }


}