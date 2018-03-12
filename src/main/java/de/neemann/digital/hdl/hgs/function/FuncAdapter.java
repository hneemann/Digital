/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;
import de.neemann.digital.hdl.hgs.Expression;

import java.util.ArrayList;

/**
 * Simple function adapter to implement a function with one argument of type long
 */
public abstract class FuncAdapter extends Function {
    /**
     * Creates a new function
     */
    public FuncAdapter() {
        super(1);
    }

    @Override
    public Object calcValue(Context c, ArrayList<Expression> args) throws EvalException {
        return f(Expression.toLong(args.get(0).value(c)));
    }

    /**
     * The function
     *
     * @param n the argument
     * @return the result
     */
    protected abstract Object f(long n);
}
