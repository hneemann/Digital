package de.neemann.digital.hdl.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PortTest {
    @Test
    public void testIsNameValid() throws Exception {
        assertTrue(Port.isNameValid("test"));
        assertTrue(Port.isNameValid("test_2"));
        assertTrue(Port.isNameValid("Test_2"));

        assertFalse(Port.isNameValid("test()++"));
        assertFalse(Port.isNameValid("test()++hallo"));
        assertFalse(Port.isNameValid("test_"));
        assertFalse(Port.isNameValid(""));
    }

}