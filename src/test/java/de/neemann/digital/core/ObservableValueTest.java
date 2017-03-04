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

}