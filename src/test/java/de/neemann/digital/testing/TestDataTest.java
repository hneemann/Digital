package de.neemann.digital.testing;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class TestDataTest extends TestCase {
    private static final String DATA1 = "A B\n0 0\n0 1\n1 0\n1 1";
    private static final String DATA2 = "A B\n0 0\n0 1\n1 0\n1 0";
    private static final String DATA3 = "A B\n0 0\n0 1\n1 0\n1 U";

    public void testSetDataNonParseable() throws Exception {
        TestData td = new TestData(DATA1);
        assertEquals(4, td.getLines().size());
        assertEquals(DATA1, td.getDataString());

        // try to set a non parsable string
        try {
            td.setDataString(DATA3);
            assertTrue(false);
        } catch (TestingDataException e) {
            assertTrue(true);
        }
        // TestData remains unchanged!
        assertEquals(DATA1, td.getDataString());
    }

    public void testSetDataParseable() throws Exception {
        TestData td = new TestData(DATA1);
        assertEquals(4, td.getLines().size());
        assertEquals(DATA1, td.getDataString());

        // try to set a parsable string
        td.setDataString(DATA2);
        // TestData has changed!
        assertEquals(DATA2, td.getDataString());
    }

}