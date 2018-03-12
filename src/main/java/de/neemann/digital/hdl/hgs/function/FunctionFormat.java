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
 * Implements the format function
 */
public class FunctionFormat extends Function {

    /**
     * Creates a new instance
     */
    public FunctionFormat() {
        super(-1);
    }

    @Override
    public Object calcValue(Context c, ArrayList<Expression> args) throws EvalException {
        if (args.size() < 1)
            throw new EvalException("format needs at least one argument");

        ArrayList<Object> eval = new ArrayList<>(args.size() - 1);
        for (int i = 1; i < args.size(); i++)
            eval.add(args.get(i).value(c));

        return String.format(Expression.toString(args.get(0).value(c)), eval.toArray());
    }
}
