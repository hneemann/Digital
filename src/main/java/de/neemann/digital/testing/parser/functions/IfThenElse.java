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
 * Implements the if-then-else function
 */
public class IfThenElse extends Function {

    /**
     * Creates a new function
     */
    public IfThenElse() {
        super(3);
    }

    @Override
    public long calcValue(Context c, ArrayList<Expression> args) throws ParserException {
        boolean cond = args.get(0).value(c)!=0;
        if (cond)
            return args.get(1).value(c);
        else
            return args.get(2).value(c);
    }
}
