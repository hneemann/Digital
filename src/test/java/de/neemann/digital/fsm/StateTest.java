/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.VectorFloat;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.TreeMap;

public class StateTest extends TestCase {

    public void testCalcExpansionForce() {
        State a = new State("a").setPosition(new VectorFloat(0, 0));
        State b = new State("b").setPosition(new VectorFloat(100, 0));

        a.calcExpansionForce(Arrays.asList(a, b));
        assertEquals(0, a.getForce().getYFloat(), 1e-5);
        final float near = a.getForce().getXFloat();
        assertTrue(near <= 0);

        b.setPosition(new VectorFloat(200, 0));
        a.calcExpansionForce(Arrays.<State>asList(a, b));
        final float far = a.getForce().getXFloat();
        assertTrue(far <= 0);
        assertTrue(Math.abs(far) < Math.abs(near));
    }
}