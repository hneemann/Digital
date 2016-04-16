package de.neemann.digital.core.memory;

import junit.framework.TestCase;

import java.io.StringReader;

/**
 * @author hneemann
 */
public class DataFieldTest extends TestCase {

    public void testGetMinimized() throws Exception {
        DataField data = new DataField(100, 8);
        data.setData(9, 1);
        data = data.getMinimized();
        assertEquals(1, data.getDataWord(9));
        data = data.getMinimized();
        assertEquals(1, data.getDataWord(9));
    }

    public void testGrow() throws Exception {
        DataField data = new DataField(100, 8);
        data.setData(9, 1);
        data = data.getMinimized();
        assertEquals(1, data.getDataWord(9));
        data.setData(30, 1);
        assertEquals(1, data.getDataWord(30));
    }

    public void testLoad() throws Exception {
        String data = "v2.0 raw\n0\n10\nAA\nFF";
        DataField df = new DataField(new StringReader(data));
        assertEquals(4, df.size());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }
}