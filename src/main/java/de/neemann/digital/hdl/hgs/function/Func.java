/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.HGSEvalException;

/**
 * A function.
 * Can be used to define a function by a lambda expression.
 */
public class Func extends FuncAdapter {
    private final Interface func;

    /**
     * Creates a new function
     *
     * @param argCount the number of arguments
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
     * A fimple function
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
