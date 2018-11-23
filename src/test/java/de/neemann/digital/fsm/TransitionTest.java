/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.VectorFloat;
import junit.framework.TestCase;

import java.util.Arrays;

public class TransitionTest extends TestCase {

    public void testCalcForceIsPreferred() {
        State a = new State("a").setPosition(new VectorFloat(0, 0));
        State b = new State("b").setPosition(new VectorFloat(10, 0));
        Transition t = new Transition(a, b, null);
        t.calcForce(10, Arrays.asList(a, b), Arrays.asList(t));
        assertEquals(0, a.getForce().getXFloat(), 1e-5);
        assertEquals(0, a.getForce().getYFloat(), 1e-5);
        assertEquals(0, b.getForce().getXFloat(), 1e-5);
        assertEquals(0, b.getForce().getYFloat(), 1e-5);
    }

    public void testCalcForceToClose() {
        State a = new State("a").setPosition(new VectorFloat(0, 0));
        State b = new State("b").setPosition(new VectorFloat(10, 0));
        Transition t = new Transition(a, b, null);
        t.calcForce(20, Arrays.asList(a, b), Arrays.asList(t));
        assertTrue(a.getForce().getXFloat() < 0);
        assertEquals(0, a.getForce().getYFloat(), 1e-5);
        assertTrue(b.getForce().getXFloat() > 0);
        assertEquals(0, b.getForce().getYFloat(), 1e-5);
    }

    public void testCalcForceToFar() {
        State a = new State("a").setPosition(new VectorFloat(0, 0));
        State b = new State("b").setPosition(new VectorFloat(10, 0));
        Transition t = new Transition(a, b, null);
        t.calcForce(5, Arrays.asList(a, b), Arrays.asList(t));
        assertTrue(a.getForce().getXFloat() > 0);
        assertEquals(0, a.getForce().getYFloat(), 1e-5);
        assertTrue(b.getForce().getXFloat() < 0);
        assertEquals(0, b.getForce().getYFloat(), 1e-5);
    }
}