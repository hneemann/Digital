/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;

/**
 * A call to a first class function.
 */
public final class FirstClassFunctionCall extends Function {
    private final FirstClassFunction func;
    private final Context capturedContext;

    /**
     * Creates a new instance
     *
     * @param func    the function
     * @param context the captured context
     */
    public FirstClassFunctionCall(FirstClassFunction func, Context context) {
        super(func.getArgs().size());
        this.func = func;
        this.capturedContext = context;
    }

    @Override
    protected Object f(Object... args) throws HGSEvalException {
        Context c = new Context(capturedContext);
        for (int i = 0; i < args.length; i++)
            c.declareVar(func.getArgs().get(i), args[i]);
        try {
            func.getStatement().execute(c);
            return null;
        } catch (ReturnException e) {
            return e.getReturnValue();
        }
    }

    /**
     * Returns from a function call.
     *
     * @param returnValue the return value
     * @throws HGSEvalException HGSEvalException
     */
    public static void returnFromFunc(Object returnValue) throws HGSEvalException {
        throw new ReturnException(returnValue);
    }

    private static final class ReturnException extends HGSEvalException {
        private final Object returnValue;

        private ReturnException(Object returnValue) {
            super("The return statement is allowed only in a function!");
            this.returnValue = returnValue;
        }

        private Object getReturnValue() {
            return returnValue;
        }
    }

}
