/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinInfo;
import de.neemann.digital.testing.parser.ParserException;
import junit.framework.TestCase;

import java.io.IOException;

/**
 */
public class TransitionsTest extends TestCase {
    private static final PinDescription[] AB = {
            new PinInfo("A", "", PinDescription.Direction.input),
            new PinInfo("B", "", PinDescription.Direction.input)};

    public void testUnique() throws IOException, ParserException {
        Transitions t;
        t = new Transitions("A B Y\n 1 1 1", AB);
        assertFalse(t.isNew());
        t = new Transitions("A B Y\n 1 1 1\n1 1 1", AB);
        assertFalse(t.isNew());
        t = new Transitions("A B Y\n 1 1 1\n1 0 1", AB);
        assertTrue(t.isNew());
    }

    public void testTwo() throws IOException, ParserException {
        Transitions t = new Transitions("A B Y\n1 1 1\n1 0 1", AB);
        assertTrue(t.isNew());

        assertEquals("A B Y\n" +
                "1 1 1\n" +
                "1 0 1\n" +
                "\n" +
                "\n" +
                "# transitions\n" +
                "1 1 1\n" +
                "1 0 1\n" +
                "1 1 1\n", t.getCompletedText());

    }

    public void testFull() throws IOException, ParserException {
        Transitions t = new Transitions("A B Y\n0 0 1\n0 1 0\n1 0 0\n1 1 1", AB);
        assertTrue(t.isNew());

        assertEquals("A B Y\n" +
                "0 0 1\n" +
                "0 1 0\n" +
                "1 0 0\n" +
                "1 1 1\n" +
                "\n" +
                "\n" +
                "# transitions\n" +
                "0 0 1\n" +
                "0 1 0\n" +
                "0 0 1\n" +
                "1 0 0\n" +
                "0 0 1\n" +
                "1 1 1\n" +
                "0 0 1\n" +
                "0 1 0\n" +
                "1 0 0\n" +
                "0 1 0\n" +
                "1 1 1\n" +
                "0 1 0\n" +
                "1 0 0\n" +
                "1 1 1\n" +
                "1 0 0\n", t.getCompletedText());

    }

}
