/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
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

    public void testHighZ() {
        ObservableValue v = new ObservableValue("z", 4);
        check(14, 1, v.set(15, 1));
        check(12, 3, v.set(15, 3));

        v.set(15, 15);
        long min = 15;
        long max = 0;
        for (int i = 0; i < 100; i++) {
            long val = v.getValue();
            if (val < min) min = val;
            if (val > max) max = val;
        }
        assertTrue(max - min > 8);
    }

    private void check(long val, long z, ObservableValue v) {
        assertEquals(val, v.getValue() & ~z);
        assertEquals(z, v.getHighZ());
    }

    public void testNoChange() {
        ObservableValue v = new ObservableValue("z", 4)
                .setToHighZ()
                .addObserverToValue(Assert::fail);
        v.set(0, 15);
        v.set(1, 15);
        v.set(2, 15);
        v.set(3, 15);
    }

    public void testChange() {
        ChangeDetector cd = new ChangeDetector();
        ObservableValue v = new ObservableValue("z", 4).addObserverToValue(cd);
        v.set(0, 2);
        assertTrue(cd.isChanged());
        v.set(2, 2);
        assertFalse(cd.isChanged());
        v.set(0, 2);
        assertFalse(cd.isChanged());
        v.set(1, 2);
        assertTrue(cd.isChanged());
    }


    private static class ChangeDetector implements Observer {
        private boolean changed;

        @Override
        public void hasChanged() {
            changed = true;
        }

        private boolean isChanged() {
            final boolean c = changed;
            changed = false;
            return c;
        }
    }
}
