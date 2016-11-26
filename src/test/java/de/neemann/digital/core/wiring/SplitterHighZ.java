package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 * Created by hneemann on 26.11.16.
 */
public class SplitterHighZ extends TestCase {

    public void testHighZ() throws NodeException {
        ObservableValue a = new ObservableValue("a", 1, true);
        ObservableValue b = new ObservableValue("b", 1);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "1,1")
                .set(Keys.OUTPUT_SPLIT, "2"));

        try {
            splitter.setInputs(ovs(a, b));
            fail("splitter high z input not detected!");
        } catch (BitsException e) {
            assertTrue(true);
        }
    }
}
