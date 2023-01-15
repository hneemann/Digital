/*
 * Copyright (c) 2023 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser.functions;

import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.Expression;
import de.neemann.digital.testing.parser.ParserException;
import junit.framework.TestCase;

import java.util.ArrayList;

public class RandomTest extends TestCase {

    public void testSmall() throws ParserException {
        Random r = new Random();
        ArrayList<Expression> args = new ArrayList<>();
        args.add(c -> 64);
        Context c = new Context();
        for (int i = 0; i < 1000; i++) {
            long v = r.calcValue(c, args);
            assertTrue(v >= 0 && v < 64);
        }
    }

    public void testLarge() throws ParserException {
        Random r = new Random();
        ArrayList<Expression> args = new ArrayList<>();
        args.add(c -> -1);
        Context c = new Context();
        int neg = 0;
        for (int i = 0; i < 10000; i++) {
            long v = r.calcValue(c, args);
            if (v < 0) neg++;
        }
        assertTrue("random generator issue", neg > 4000 && neg < 6000);
    }

}