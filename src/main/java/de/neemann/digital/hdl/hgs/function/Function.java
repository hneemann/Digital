/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;
import de.neemann.digital.hdl.hgs.Expression;

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
     * Calculates the value
     *
     * @param c    the context
     * @param args the arguments
     * @return the value
     * @throws EvalException EvalException
     */
    public abstract Object calcValue(Context c, ArrayList<Expression> args) throws EvalException;
}
