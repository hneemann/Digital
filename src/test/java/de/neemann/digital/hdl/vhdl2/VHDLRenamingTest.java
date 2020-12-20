/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import junit.framework.TestCase;

public class VHDLRenamingTest extends TestCase {

    public void testCheckName() {
        VHDLRenaming r = new VHDLRenaming();
        assertEquals("a", r.checkName("a"));
        assertEquals("n0a", r.checkName("0a"));
        assertEquals("p_in", r.checkName("in"));
        assertEquals("a_u_in", r.checkName("a&u(in"));
        assertEquals("a_in", r.checkName("a&ü(in"));
        assertEquals("a_o_o", r.checkName("a\"o\"o"));
        assertEquals("o", r.checkName("\"o\""));
        assertEquals("o", r.checkName("_o_"));
        assertEquals("notQ", r.checkName("/Q"));
        assertEquals("notQ", r.checkName("!Q"));
        assertEquals("notQ", r.checkName("~Q"));
        assertEquals("aleb", r.checkName("a<b"));
        assertEquals("agrb", r.checkName("a>b"));
        assertEquals("aeqb", r.checkName("a=b"));
        assertEquals("a_b", r.checkName("a\\_b"));
        assertEquals("a_b", r.checkName("a\\^b"));
    }
}