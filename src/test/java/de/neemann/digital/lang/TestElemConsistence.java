package de.neemann.digital.lang;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.FanIn;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.memory.LookUpTable;
import de.neemann.digital.core.wiring.Decoder;
import de.neemann.digital.core.wiring.Demultiplexer;
import de.neemann.digital.core.wiring.PriorityEncoder;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.GraphicSVG;
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
    public void testConsistence() throws NodeException, PinException {
        ElementLibrary library = new ElementLibrary();
        for (ElementLibrary.ElementContainer e : library) {
            ElementTypeDescription etd = e.getDescription();

            String key = "elem_" + etd.getName();
            assertNotNull("Key "+key+" not found", Lang.getNull(key));

            if (Lang.getNull(key + "_tt") == null)
                missing(key + "_tt");

            if (isNormalElement(etd)) {
                checkPins(key, etd.getInputDescription(new ElementAttributes()));
                checkPins(key, etd.getOutputDescriptions(new ElementAttributes()));
            }
        }
    }

    private void checkPins(String key, PinDescriptions pins) {
        for (PinDescription in : pins) {
            final String pinKey = key + "_pin_" + in.getName();
            String str = Lang.getNull(pinKey);
            if (str == null)
                missing(pinKey);
        }
    }

    private void missing(String key) {
        final String xml = GraphicSVG.escapeXML(key);
        System.out.println("<string name=\"" + xml + "\">" + xml + "</string>");
        fail("key '" + key + "' is missing!");
    }

    private boolean isNormalElement(ElementTypeDescription etd) {
        Element e = etd.createElement(new ElementAttributes());

        return !(e instanceof FanIn
                || e instanceof Demultiplexer
                || e instanceof Decoder
                || e instanceof PriorityEncoder
                || e instanceof Splitter
                || e instanceof LookUpTable);
    }

}
