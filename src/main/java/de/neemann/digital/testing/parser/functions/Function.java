/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser.functions;

import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.Expression;
import de.neemann.digital.testing.parser.ParserException;

import java.util.ArrayList;

/**
 * A simple mathematical function
 */
public abstract class Function {

    private final int argCount;

    /**
     * Creates a new function
     *
     * @param argCount the number of arguments
     */
    public Function(int argCount) {
        this.argCount = argCount;
    }

    /**
     * @return the number of required arguments
     */
    public int getArgCount() {
        return argCount;
    }

    /**
     * calculates the value
     *
     * @param c    the context
     * @param args the arguments
     * @return the value
     * @throws ParserException ParserException
     */
    public abstract long calcValue(Context c, ArrayList<Expression> args) throws ParserException;
}
