/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.data.Value;
import junit.framework.TestCase;

/**
 */
public class ValueTest extends TestCase {

    public void testSimple() throws Exception {
        Value v = new Value("X");
        assertEquals(Value.Type.DONTCARE, v.getType());

        v = new Value("Z");
        assertEquals(Value.Type.HIGHZ, v.getType());

        v = new Value("C");
        assertEquals(Value.Type.CLOCK, v.getType());
        assertEquals(1, v.getValue());

        v = new Value("8");
        assertEquals(Value.Type.NORMAL, v.getType());
        assertEquals(8, v.getValue());
    }

    public void testCompare() throws Exception {
        assertTrue(new Value("X").isEqualTo(new Value("X")));
        assertTrue(new Value("Z").isEqualTo(new Value("X")));
        assertTrue(new Value("X").isEqualTo(new Value("Z")));
        assertTrue(new Value("1").isEqualTo(new Value("X")));
        assertTrue(new Value("X").isEqualTo(new Value("1")));

        assertTrue(new Value("Z").isEqualTo(new Value("Z")));
        assertFalse(new Value("Z").isEqualTo(new Value("1")));
        assertFalse(new Value("1").isEqualTo(new Value("Z")));

        assertTrue(new Value("0").isEqualTo(new Value("0")));
        assertTrue(new Value("1").isEqualTo(new Value("1")));
        assertFalse(new Value("0").isEqualTo(new Value("1")));
    }

    public void testToString() throws Exception {
        assertEquals("X",new Value("X").toString());
        assertEquals("Z",new Value("Z").toString());
        assertEquals("2",new Value("2").toString());
        assertEquals("C",new Value("C").toString());
    }
}
