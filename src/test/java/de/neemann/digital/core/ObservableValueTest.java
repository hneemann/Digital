package de.neemann.digital.core;

import junit.framework.TestCase;

/**
 * Created by hneemann on 04.03.17.
 */
public class ObservableValueTest extends TestCase {

    public void testSetValue1() throws Exception {
        ObservableValue v = new ObservableValue("z", 64);
        v.setValue(5);
        assertEquals(5, v.getValue());
    }

    public void testSetValue2() throws Exception {
        ObservableValue v = new ObservableValue("z", 63);
        v.setValue(-1);
        assertEquals((1L << 63) - 1, v.getValue());
    }

    public void testSetValue3() throws Exception {
        ObservableValue v = new ObservableValue("z", 62);
        v.setValue(-1);
        assertEquals((1L << 62) - 1, v.getValue());
    }

    public void testSigned() {
        ObservableValue v = new ObservableValue("z", 4);
        assertEquals(0, v.setValue(0).getValueSigned());
        assertEquals(1, v.setValue(1).getValueSigned());
        assertEquals(7, v.setValue(7).getValueSigned());
        assertEquals(-8, v.setValue(8).getValueSigned());
        assertEquals(-1, v.setValue(15).getValueSigned());
        assertEquals(-1, v.setValue(-1).getValueSigned());

        v = new ObservableValue("z", 1);
        assertEquals(0, v.setValue(0).getValueSigned());
        assertEquals(-1, v.setValue(1).getValueSigned());

        v = new ObservableValue("z", 8);
        assertEquals(127, v.setValue(127).getValueSigned());
        assertEquals(-128, v.setValue(128).getValueSigned());
        assertEquals(255, v.setValue(-1).getValue());
        assertEquals(-1, v.setValue(-1).getValueSigned());
    }

}