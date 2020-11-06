/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import junit.framework.TestCase;

public class ResolveGenericsTest extends TestCase {

    public void testEscapeString() {
        check("Test", "Test");
        check("\\\\", "\\");
        check("\\n", "\n");
        check("\\r", "\r");
        check("\\t", "\t");
        check("\\\"Test\\\"", "\"Test\"");
    }

    private void check(String expected, String found) {
        StringBuilder sb = new StringBuilder();
        ResolveGenerics.escapeString(sb, found);
        assertEquals(expected, sb.toString());
    }
}