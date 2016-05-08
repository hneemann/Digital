package de.neemann.digital.analyse;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class TruthTableTest extends TestCase {

    public void testGetRows() throws Exception {
        TruthTable t = new TruthTable(3);

        assertEquals(8, t.getRows());
    }

}