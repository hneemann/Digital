/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Expression;

import java.util.ArrayList;

/**
 * Description of a basic function.
 * The arguments to the function are expressions not yet evaluated.
 * In most cases its easier to override the {@link Function} class.
 */
public abstract class InnerFunction {

    private final int argCount;

    /**
     * Creates a new function
     *
     * @param argCount the number of arguments, The value -1 means any number is allowed!
     */
    public InnerFunction(int argCount) {
        this.argCount = argCount;
    }

    /**
     * @return the number of required arguments
     */
    public int getArgCount() {
        return argCount;
    }

    /**
     * Calculates the value.
     * Don't call this function from your java code!
     *
     * @param c    the context
     * @param args the arguments
     * @return the value
     * @throws HGSEvalException HGSEvalException
     */
    public abstract Object call(Context c, ArrayList<Expression> args) throws HGSEvalException;
}
