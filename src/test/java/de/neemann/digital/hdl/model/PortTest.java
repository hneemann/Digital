package de.neemann.digital.hdl.model;

import junit.framework.TestCase;

public class PortTest extends TestCase {

    public void testIsNameValid() throws Exception {
        assertTrue(Port.isNameValid("test"));
        assertTrue(Port.isNameValid("test_2"));
        assertTrue(Port.isNameValid("Test_2"));

        assertFalse(Port.isNameValid("test()++"));
        assertFalse(Port.isNameValid("test()++hallo"));
        assertFalse(Port.isNameValid("test_"));
        assertFalse(Port.isNameValid(""));
    }

    public void testDuplicateName() throws HDLException {
        Ports p = new Ports();
        p.add(new Port("a", Port.Direction.out));
        p.add(new Port("b", Port.Direction.out));

        try {
            p.add(new Port("A", Port.Direction.out));
            fail();
        } catch (HDLException e) {
            // expected
        }
    }

}