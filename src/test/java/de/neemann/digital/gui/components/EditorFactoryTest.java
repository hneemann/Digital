/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

public class EditorFactoryTest extends TestCase {

    public void testKeyConstrains() {
        ElementAttributes attr= new ElementAttributes();
        assertEquals(attr.get(Keys.INPUT_COUNT), attr.get(Keys.ADDR_BITS));
        // see comments in EditorFactory$DataFieldEditor
    }
}