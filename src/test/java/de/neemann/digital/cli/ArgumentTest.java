/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.CLIException;
import junit.framework.TestCase;

public class ArgumentTest extends TestCase {

    public void testString() throws CLIException {
        Argument<String> a = new Argument<>("n", "a", false);
        assertEquals("a", a.get());
        a.setString("hello");
        assertEquals("hello", a.get());
    }

    public void testBool() throws CLIException {
        Argument<Boolean> a = new Argument<>("n", true, false);
        assertTrue(a.get());
        a.setString("false");
        assertFalse(a.get());
        a.setString("true");
        assertTrue(a.get());
        a.setString("0");
        assertFalse(a.get());
        a.setString("1");
        assertTrue(a.get());
        a.setString("no");
        assertFalse(a.get());
        a.setString("yes");
        assertTrue(a.get());

        try {
            a.setString("foo");
            fail();
        } catch (CLIException e) {
        }
    }

    public void testToggle() throws CLIException {
        Argument<Boolean> a = new Argument<>("flag", false, false);
        assertTrue(a.isBool());
        assertFalse(a.get());
        a.toggle();
        assertTrue(a.get());
    }

    public void testInteger() throws CLIException {
        Argument<Integer> a = new Argument<>("n", 2, false);
        assertEquals(2, (int) a.get());
        a.setString("5");
        assertEquals(5, (int) a.get());

        try {
            a.setString("foo");
            fail();
        } catch (CLIException e) {
        }

    }
}