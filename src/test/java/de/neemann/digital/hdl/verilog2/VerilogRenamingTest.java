/*
 * Copyright (c) 2018 Ivan Deras.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import junit.framework.TestCase;

public class VerilogRenamingTest extends TestCase {

    public void testCheckName() {
        VerilogRenaming r = new VerilogRenaming();
        assertEquals("a", r.checkName("a"));
        assertEquals("\\0a ", r.checkName("0a"));
        assertEquals("\\input ", r.checkName("input"));
        assertEquals("\\a&u(in ", r.checkName("a&u(in"));
        assertEquals("\\a&ü(in ", r.checkName("a&ü(in"));
        assertEquals("\\a\"o\"o ", r.checkName("a\"o\"o"));
        assertEquals("\\\"o\" ", r.checkName("\"o\""));
        assertEquals("_o_", r.checkName("_o_"));
        assertEquals("\\/Q ", r.checkName("/Q"));
        assertEquals("\\!Q ", r.checkName("!Q"));
        assertEquals("\\~Q ", r.checkName("~Q"));
        assertEquals("\\a<b ", r.checkName("a<b"));
        assertEquals("\\a>b ", r.checkName("a>b"));
        assertEquals("\\a=b ", r.checkName("a=b"));
        assertEquals("a_b", r.checkName("a\\_b"));
        assertEquals("\\a^b ", r.checkName("a\\^b"));
    }
}