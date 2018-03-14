/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Expression;

import java.util.ArrayList;

/**
 * Simple function adapter to implement a function with already evaluated arguments
 */
public abstract class FuncAdapter extends Function {
    /**
     * Creates a new function
     *
     * @param argCount the number of arguments
     */
    public FuncAdapter(int argCount) {
        super(argCount);
    }

    @Override
    public Object calcValue(Context c, ArrayList<Expression> args) throws HGSEvalException {
        if (getArgCount() != args.size())
            throw new HGSEvalException("wrong number of arguments! found: " + args.size() + ", expected: " + args.size());

        Object[] data = new Object[args.size()];
        for (int i = 0; i < args.size(); i++)
            data[i] = args.get(i).value(c);
        return f(data);
    }

    /**
     * The function
     *
     * @param args the evaluated arguments
     * @return the result
     * @throws HGSEvalException HGSEvalException
     */
    protected abstract Object f(Object... args) throws HGSEvalException;
}
