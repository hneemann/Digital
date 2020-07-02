/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import com.thoughtworks.xstream.XStream;
import de.neemann.digital.XStreamValid;
import junit.framework.TestCase;

/**
 */
public class DataFieldConverterTest extends TestCase {

    private XStream getxStream() {
        XStream xStream = new XStreamValid();
        xStream.registerConverter(new DataFieldConverter());
        xStream.alias("dataField", DataField.class);
        xStream.alias("test", Test.class);
        return xStream;
    }

    public void testMarshal() throws Exception {
        DataField d = new DataField(1000);
        for (int i = 0; i < 10; i++)
            d.setData(i, i);

        XStream xStream = getxStream();

        String xml = xStream.toXML(d);
        assertEquals("<?xml version=\"1.0\" ?><dataField>0,1,2,3,4,5,6,7,8,9</dataField>", xml);
    }

    public void testUnmarshal() throws Exception {
        XStream xStream = getxStream();

        DataField df = (DataField) xStream.fromXML("<dataField size=\"1000\">0,1,2,3,4,5,6,7,8,9</dataField>");

        assertEquals(10, df.getData().length);
        for (int i = 0; i < 10; i++)
            assertEquals(i, df.getDataWord(i));
    }

    private static class Test {
        private DataField d1;
        private DataField d2;

        public Test(DataField d1, DataField d2) {
            this.d1 = d1;
            this.d2 = d2;
        }
    }

    public void testMarshalObj() throws Exception {
        final DataField d1 = new DataField(20);
        d1.setData(0, 1);
        d1.setData(5, 2);

        final DataField d2 = new DataField(20);
        d2.setData(0, 3);
        d2.setData(8, 4);
        Test t = new Test(d1, d2);

        XStream xs = getxStream();
        String xml = xs.toXML(t);
        assertEquals("<?xml version=\"1.0\" ?><test>" +
                "<d1>1,0,0,0,0,2</d1>" +
                "<d2>3,0,0,0,0,0,0,0,4</d2>" +
                "</test>", xml);

    }

    public void testUnarshalObj() throws Exception {
        XStream xs = getxStream();
        Test t = (Test) xs.fromXML("<test>\n" +
                "  <d1>1,0,0,0,0,2</d1>\n" +
                "  <d2>3,0,0,0,0,0,0,0,4</d2>\n" +
                "</test>");

        assertEquals(6, t.d1.getData().length);
        assertEquals(1, t.d1.getDataWord(0));
        assertEquals(2, t.d1.getDataWord(5));
        assertEquals(9, t.d2.getData().length);
        assertEquals(3, t.d2.getDataWord(0));
        assertEquals(4, t.d2.getDataWord(8));
    }


    public void testMarshalMuch() throws Exception {
        DataField d = new DataField(1000);
        for (int i = 0; i < 100; i++)
            d.setData(i, i);

        XStream xStream = getxStream();

        String xml = xStream.toXML(d);
        assertEquals("<?xml version=\"1.0\" ?><dataField>0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,10,11,12,13,14,15,16,17,18,19,1a,1b,1c,1d,1e,1f,20,\n" +
                "21,22,23,24,25,26,27,28,29,2a,2b,2c,2d,2e,2f,30,31,32,33,34,35,36,37,38,39,3a,3b,\n" +
                "3c,3d,3e,3f,40,41,42,43,44,45,46,47,48,49,4a,4b,4c,4d,4e,4f,50,51,52,53,54,55,56,\n" +
                "57,58,59,5a,5b,5c,5d,5e,5f,60,61,62,63</dataField>", xml);
    }

    public void testUnmarshalMuch() throws Exception {
        XStream xStream = getxStream();

        DataField df = (DataField) xStream.fromXML("<dataField>0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,10,11,12,13,14,15,16,17,18,19,1a,1b,1c,1d,1e,1f,20,\n" +
                "21,22,23,24,25,26,27,28,29,2a,2b,2c,2d,2e,2f,30,31,32,33,34,35,36,37,38,39,3a,3b,\n" +
                "3c,3d,3e,3f,40,41,42,43,44,45,46,47,48,49,4a,4b,4c,4d,4e,4f,50,51,52,53,54,55,56,\n" +
                "57,58,59,5a,5b,5c,5d,5e,5f,60,61,62,63</dataField>");

        assertEquals(100, df.getData().length);
        for (int i = 0; i < 100; i++)
            assertEquals(i, df.getDataWord(i));
    }


    public void testUnmarchalOld() {
        XStream xStream = getxStream();

        DataField df = (DataField) xStream.fromXML("<dataField>\n" +
                "  <size>1000</size>\n" +
                "  <long>0</long>\n" +
                "  <long>1</long>\n" +
                "  <long>2</long>\n" +
                "  <long>3</long>\n" +
                "  <long>4</long>\n" +
                "  <long>5</long>\n" +
                "  <long>6</long>\n" +
                "  <long>7</long>\n" +
                "  <long>8</long>\n" +
                "  <long>9</long>\n" +
                "</dataField>");
        assertEquals(1000, df.getData().length);
        for (int i = 0; i < 10; i++)
            assertEquals(i, df.getDataWord(i));
    }

}
