package de.neemann.digital.testing;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class TestDataParserTest extends TestCase {

    public void testOk() throws TestingDataException {
        TestDataParser td = new TestDataParser("A B\n0 1\n1 0\nX x").parse();
        assertEquals(2,td.getNames().size());
        assertEquals(3,td.getLines().size());

        assertEquals(0, td.getLines().get(0)[0].getValue());
        assertEquals(Value.Type.NORMAL,td.getLines().get(0)[0].getType());

        assertEquals(1, td.getLines().get(0)[1].getValue());
        assertEquals(Value.Type.NORMAL,td.getLines().get(0)[1].getType());

        assertEquals(1, td.getLines().get(1)[0].getValue());
        assertEquals(Value.Type.NORMAL,td.getLines().get(1)[0].getType());

        assertEquals(0, td.getLines().get(1)[1].getValue());
        assertEquals(Value.Type.NORMAL,td.getLines().get(1)[1].getType());

        assertEquals(Value.Type.DONTCARE,td.getLines().get(2)[0].getType());
        assertEquals(Value.Type.DONTCARE,td.getLines().get(2)[1].getType());
    }

    public void testMissingValue()  {
        try {
            new TestDataParser("A B\n0 0\n1").parse();
            assertTrue(false);
        } catch (TestingDataException e) {
            assertTrue(true);
        }
    }

    public void testInvalidValue()  {
        try {
            new TestDataParser("A B\n0 0\n1 u").parse();
            assertTrue(false);
        } catch (TestingDataException e) {
            assertTrue(true);
        }
    }

    public void testClock() throws Exception {
        TestDataParser td = new TestDataParser("A B\nC 1\nC 0").parse();
        assertEquals(2,td.getNames().size());
        assertEquals(2,td.getLines().size());

        assertEquals(Value.Type.CLOCK, td.getLines().get(0)[0].getType());
        assertEquals(1, td.getLines().get(0)[1].getValue());
        assertEquals(Value.Type.CLOCK, td.getLines().get(1)[0].getType());
        assertEquals(0, td.getLines().get(1)[1].getValue());
    }
}