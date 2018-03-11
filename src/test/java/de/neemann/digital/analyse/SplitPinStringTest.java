/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Signal;
import junit.framework.TestCase;

public class SplitPinStringTest extends TestCase {

    public void testPins() {
        SplitPinString p = SplitPinString.create(new Signal("a", null).setPinNumber("u1,u2 , u3 , u4"));
        assertEquals("u1",p.getPin(0));
        assertEquals("u2",p.getPin(1));
        assertEquals("u3",p.getPin(2));
        assertEquals("u4",p.getPin(3));
        assertEquals(null,p.getPin(4));
    }

    public void testEmpty() {
        SplitPinString p = SplitPinString.create(new Signal("a", null));
        assertEquals(null,p.getPin(0));
        assertEquals(null,p.getPin(1));
        assertEquals(null,p.getPin(2));
    }

}
