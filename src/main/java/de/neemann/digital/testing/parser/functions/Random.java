/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser.functions;

import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.Expression;
import de.neemann.digital.testing.parser.ParserException;

import java.util.ArrayList;

/**
 * Generates a random number.
 * Useful to create regression tests.
 */
public class Random extends Function {

    /**
     * Creates a new function
     */
    public Random() {
        super(1);
    }

    @Override
    public long calcValue(Context c, ArrayList<Expression> args) throws ParserException {
        return (long) (Math.random() * args.get(0).value(c));
    }
}
