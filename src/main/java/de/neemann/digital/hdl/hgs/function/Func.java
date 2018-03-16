/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.HGSEvalException;

/**
 * Can be used to define a function by a lambda expression.
 */
public class Func extends Function {
    private final Interface func;

    /**
     * Creates a new function
     *
     * @param argCount the number of arguments, The value -1 means any number is allowed!
     * @param func     the function
     */
    public Func(int argCount, Interface func) {
        super(argCount);
        this.func = func;
    }

    @Override
    protected Object f(Object... args) throws HGSEvalException {
        return func.f(args);
    }

    /**
     * A simple function
     */
    public interface Interface {
        /**
         * Evaluates the function
         *
         * @param args the arguments
         * @return the result
         * @throws HGSEvalException HGSEvalException
         */
        Object f(Object... args) throws HGSEvalException;
    }
}
