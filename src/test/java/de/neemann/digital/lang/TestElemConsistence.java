package de.neemann.digital.lang;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.FanIn;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.library.ElementLibrary;
import junit.framework.TestCase;

/**
 * Created by hneemann on 19.11.16.
 */
public class TestElemConsistence extends TestCase {

    /**
     * Checks if element descriptions are complete
     *
     * @throws NodeException
     */
    public void testConsistence() throws NodeException {
        ElementLibrary library = new ElementLibrary();
        for (ElementLibrary.ElementContainer e : library) {
            ElementTypeDescription etd = e.getDescription();

            String key = "elem_" + etd.getName();
            assertNotNull(Lang.getNull(key));

            //assertNotNull("Missing tooltip for "+key, Lang.getNull(key+"_tt"));
            if (Lang.getNull(key + "_tt") == null)
                System.out.println("missing key " + key + "_tt");

            if (!(etd.createElement(new ElementAttributes()) instanceof FanIn)) {
                PinDescriptions inputs = etd.getInputDescription(new ElementAttributes());
                for (PinDescription in : inputs) {
                    final String inputKey = key + "_pin_" + in.getName();
                    String str = Lang.getNull(inputKey);
                    //assertNotNull("missing key " + inputKey, str);
                    if (str == null)
                        System.out.println("missing key " + inputKey);

                }
            }
        }
    }

}
