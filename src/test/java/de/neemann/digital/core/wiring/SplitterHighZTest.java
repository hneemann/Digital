package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

import static de.neemann.digital.TestExecuter.HIGHZ;
import static de.neemann.digital.core.ObservableValues.ovs;

/**
 * Created by hneemann on 26.11.16.
 */
public class SplitterHighZTest extends TestCase {

    public void testHighZError() throws NodeException {
        ObservableValue a = new ObservableValue("a", 1, true);
        ObservableValue b = new ObservableValue("b", 1);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "1,1")
                .set(Keys.OUTPUT_SPLIT, "2"));

        try {
            splitter.setInputs(ovs(a, b));
            fail("splitter high z input not detected!");
        } catch (NodeException e) {
            assertTrue(true);
        }
    }

    public void testHighZNotEnabled() throws NodeException {
        ObservableValue a = new ObservableValue("a", 2, true);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "2")
                .set(Keys.OUTPUT_SPLIT, "1,1"));

        try {
            splitter.setInputs(ovs(a));
            fail("splitter high z input not detected!");
        } catch (NodeException e) {
            assertTrue(true);
        }
    }

    public void testHighZEnabled() throws NodeException, PinException {
        ObservableValue a = new ObservableValue("a", 2, true);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.IS_HIGH_Z, true)
                .set(Keys.INPUT_SPLIT, "2")
                .set(Keys.OUTPUT_SPLIT, "1,1"));

        splitter.setInputs(ovs(a));

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(2, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a).setOutputsOf(splitter);
        sc.check(0, 0, 0);
        sc.check(1, 1, 0);
        sc.check(2, 0, 1);
        sc.check(3, 1, 1);
        sc.checkZ(HIGHZ, HIGHZ, HIGHZ);
    }

}
