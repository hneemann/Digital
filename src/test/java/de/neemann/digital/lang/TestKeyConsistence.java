package de.neemann.digital.lang;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.FanIn;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.memory.LookUpTable;
import de.neemann.digital.core.wiring.Decoder;
import de.neemann.digital.core.wiring.Demultiplexer;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.GraphicSVG;
import de.neemann.digital.draw.library.ElementLibrary;
import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * Created by hneemann on 19.11.16.
 */
public class TestKeyConsistence extends TestCase {

    /**
     * Checks if key descriptions are complete
     *
     * @throws NodeException
     * @throws PinException
     * @throws IllegalAccessException
     */
    public void testConsistence() throws NodeException, PinException, IllegalAccessException {
        for (Field f : Keys.class.getDeclaredFields()) {
            Key key = ((Key) f.get(null));
            checkKey(key.getLangKey());
            checkKey(key.getLangKey() + "_tt");

            if (key instanceof Key.KeyEnum) {
                Key.KeyEnum ke = (Key.KeyEnum) key;
                for (Enum v : ke.getValues())
                    checkKey(ke.getLangKey(v));
            }
        }
    }

    private void checkKey(String key) {
        String str = Lang.getNull(key);
        if (str == null)
            missing(key);
    }

    private void missing(String key) {
        final String xml = GraphicSVG.escapeXML(key);
        System.out.println("<string name=\"" + xml + "\">" + xml + "</string>");
        fail("key '" + key + "' is missing!");
    }

}
