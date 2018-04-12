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
 * Class to implement a function with already evaluated arguments.
 * Its the easiest way to implement a pure function, which means a mathematical function
 * which only uses the arguments given to the function.
 */
public abstract class Function extends InnerFunction {
    /**
     * Creates a new function
     *
     * @param argCount the number of arguments, The value -1 means any number is allowed!
     */
    public Function(int argCount) {
        super(argCount);
    }

    @Override
    public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
        Object[] data = new Object[args.size()];
        for (int i = 0; i < args.size(); i++)
            data[i] = args.get(i).value(c);
        return f(data);
    }

    /**
     * Use this method to call the function from your java code.
     *
     * @param args the arguments of this function
     * @return the function result
     * @throws HGSEvalException HGSEvalException
     */
    public Object call(Object... args) throws HGSEvalException {
        if (getArgCount() >= 0 && getArgCount() != args.length)
            throw new HGSEvalException("wrong number of arguments! found: " + args.length + ", expected: " + getArgCount());
        return f(args);
    }

    /**
     * Evaluates this function.
     * If you did not use -1 as the argument number, you don't have to check the argument count.
     * If you used -1 as the argument number, you have!
     *
     * @param args the arguments
     * @return the result
     * @throws HGSEvalException HGSEvalException
     */
    protected abstract Object f(Object... args) throws HGSEvalException;
}
