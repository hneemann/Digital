/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.Expression;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Value;

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
    public Object calcValue(Context c, ArrayList<Expression> args) throws HGSEvalException {
        return format(c, args);
    }

    /**
     * Formats the gicen string like printf does
     *
     * @param c    the context
     * @param args the arbuments
     * @return the formatted string
     * @throws HGSEvalException HGSEvalException
     */
    public static String format(Context c, ArrayList<Expression> args) throws HGSEvalException {
        if (args.size() < 1)
            throw new HGSEvalException("Format needs at least one argument!");

        ArrayList<Object> eval = new ArrayList<>(args.size() - 1);
        for (int i = 1; i < args.size(); i++)
            eval.add(args.get(i).value(c));

        return String.format(Value.toString(args.get(0).value(c)), eval.toArray());
    }
}
