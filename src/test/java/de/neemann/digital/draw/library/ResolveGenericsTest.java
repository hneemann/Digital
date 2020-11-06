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