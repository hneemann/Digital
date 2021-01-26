/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.GraphicSVG;
import de.neemann.digital.gui.components.EditorFactory;
import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 *
 */
public class TestKeyConsistence extends TestCase {

    /**
     * Checks if key descriptions are complete
     */
    public void testConsistence() {
        for (Key key : Keys.getKeys()) {
            checkKey(key.getLangKey());
            checkKey(key.getLangKey() + "_tt");

            if (key instanceof Key.KeyEnum) {
                Key.KeyEnum ke = (Key.KeyEnum) key;
                if (!ke.usesToString())
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
