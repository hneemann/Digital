/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import junit.framework.TestCase;

public class LabelGeneratorTest extends TestCase {

    public void testGenerator() {
        LabelGenerator gen = new LabelGenerator('A', 'B', 'C');
        assertEquals("A", gen.createLabel());
        assertEquals("B", gen.createLabel());
        assertEquals("C", gen.createLabel());
        assertEquals("A2", gen.createLabel());
        assertEquals("B2", gen.createLabel());
        assertEquals("C2", gen.createLabel());
        assertEquals("A3", gen.createLabel());
        assertEquals("B3", gen.createLabel());
        assertEquals("C3", gen.createLabel());
    }
}
