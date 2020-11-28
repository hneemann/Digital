/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.io.InValue;
import junit.framework.TestCase;

public class ValueTest extends TestCase {

    public void testSize() {
        assertEquals(3, new Value(-1, 2).getValue());
        assertEquals(1, new Value(1, 2).getValue());
        assertEquals(-1, new Value(-1, 2).getValueSigned());
        assertEquals(-1, new Value(3, 2).getValueSigned());
    }

    public void testFromInValue() throws Bits.NumberFormatException {
        assertEquals("5", new Value(new InValue("5"), 4).toString());
        assertEquals("Z", new Value(new InValue("z"), 4).toString());
        assertEquals("Z", IntFormat.hex.formatToEdit(new Value(new InValue("z"), 4)));
    }
}
