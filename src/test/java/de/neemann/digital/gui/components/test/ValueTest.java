package de.neemann.digital.gui.components.test;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class ValueTest extends TestCase {

    public void testSimple() throws Exception {
        Value v = new Value("X");
        assertTrue(v.isDontCare());

        v = new Value("Z");
        assertTrue(v.isHighZ());
        assertFalse(v.isDontCare());

        v = new Value("8");
        assertFalse(v.isHighZ());
        assertFalse(v.isDontCare());
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
    }
}