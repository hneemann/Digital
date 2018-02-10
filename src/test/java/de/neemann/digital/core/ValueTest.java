package de.neemann.digital.core;

import junit.framework.TestCase;

public class ValueTest extends TestCase {

    public void testSize() {
        assertEquals(3, new Value(-1, 2).getValue());
        assertEquals(1, new Value(1, 2).getValue());
        assertEquals(-1, new Value(-1, 2).getValueSigned());
        assertEquals(-1, new Value(3, 2).getValueSigned());
    }

}