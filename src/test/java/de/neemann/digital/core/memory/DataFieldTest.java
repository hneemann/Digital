package de.neemann.digital.core.memory;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class DataFieldTest extends TestCase {

    public void testGetMinimized() throws Exception {
        DataField data = new DataField(100);
        data.setData(9, 1);
        data = data.getMinimized();
        assertEquals(1, data.getData(9));
        data = data.getMinimized();
        assertEquals(1, data.getData(9));
    }

    public void testGrow() throws Exception {
        DataField data = new DataField(100);
        data.setData(9, 1);
        data = data.getMinimized();
        assertEquals(1, data.getData(9));
        data.setData(30, 1);
        assertEquals(1, data.getData(30));
    }

}