/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

import junit.framework.TestCase;

public class CharDeleterTest extends TestCase {

    public void testSimple() {
        CharDeleter cd = new CharDeleter("test", 4).delete();
        assertEquals("tes", cd.getText());
        assertEquals(3, cd.getPos());
    }

    public void testLineFeed1() {
        CharDeleter cd = new CharDeleter("\n\n", 0).delete();
        assertEquals("\n", cd.getText());
        assertEquals(0, cd.getPos());
    }

    public void testLineFeed2() {
        CharDeleter cd = new CharDeleter("\ntest\n", 0).delete();
        assertEquals("\ntest", cd.getText());
        assertEquals(4, cd.getPos());
    }

    public void testLineFeed3() {
        CharDeleter cd = new CharDeleter("test\n", 0).delete();
        assertEquals("test", cd.getText());
        assertEquals(4, cd.getPos());
    }

    public void testLineFeed4() {
        CharDeleter cd = new CharDeleter("test\ntest\n", 0).delete();
        assertEquals("test\ntest", cd.getText());
        assertEquals(4, cd.getPos());
    }

}