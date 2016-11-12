package de.neemann.digital.core.pld;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * Created by hneemann on 01.11.16.
 */
public class DirectionalDiodeTest extends TestCase {

    public void testDiodeForeward() throws Exception {
        ObservableValue a = new ObservableValue("a", 1);

        DiodeForeward diodeForeward = new DiodeForeward(new ElementAttributes());
        diodeForeward.setInputs(a.asList());
        diodeForeward.init(null);

        ObservableValues outputs = diodeForeward.getOutputs();
        assertEquals(1, outputs.size());

        ObservableValue output = outputs.get(0);

        a.setBool(true);
        assertEquals(false, output.isHighZ());
        assertEquals(1, output.getValue());
        a.setBool(false);
        assertEquals(true, output.isHighZ());
    }

    public void testDiodeBackward() throws Exception {
        ObservableValue a = new ObservableValue("a", 1);

        DiodeBackward diodeBackward = new DiodeBackward(new ElementAttributes());
        diodeBackward.setInputs(a.asList());
        diodeBackward.init(null);

        ObservableValues outputs = diodeBackward.getOutputs();
        assertEquals(1, outputs.size());

        ObservableValue output = outputs.get(0);

        a.setBool(true);
        assertEquals(true, output.isHighZ());
        a.setBool(false);
        assertEquals(false, output.isHighZ());
        assertEquals(0, output.getValue());
    }


}