package de.neemann.digital.gui.components.test;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class TestDataParserTest extends TestCase {

    public void testOk() throws DataException {
        TestDataParser td = new TestDataParser("A B\n0 1\n1 0\nX x").parse();
        assertEquals(2,td.getNames().size());
        assertEquals(3,td.getLines().size());

        assertEquals(0, td.getLines().get(0)[0]);
        assertEquals(1, td.getLines().get(0)[1]);
        assertEquals(1, td.getLines().get(1)[0]);
        assertEquals(0, td.getLines().get(1)[1]);
        assertEquals(-1, td.getLines().get(2)[0]);
        assertEquals(-1, td.getLines().get(2)[1]);
    }

    public void testMissingValue()  {
        try {
            new TestDataParser("A B\n0 0\n1").parse();
            assertTrue(false);
        } catch (DataException e) {
            assertTrue(true);
        }
    }

    public void testInvalidValue()  {
        try {
            new TestDataParser("A B\n0 0\n1 u").parse();
            assertTrue(false);
        } catch (DataException e) {
            assertTrue(true);
        }
    }

}