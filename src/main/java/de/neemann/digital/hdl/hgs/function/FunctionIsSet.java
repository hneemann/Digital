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
 * Check if access to a variable is possible without an error.
 */
public class FunctionIsSet extends Function {

    /**
     * Creates a new function
     */
    public FunctionIsSet() {
        super(1);
    }

    @Override
    public Object calcValue(Context c, ArrayList<Expression> args) {
        try {
            args.get(0).value(c);
            return true;
        } catch (EvalException e) {
            return false;
        }
    }
}
