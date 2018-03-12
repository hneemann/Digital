/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.util.ArrayList;

/**
 * callable first class function
 */
public class FirstClassFunction {
    private final ArrayList<String> args;
    private final Statement st;

    /**
     * Creates a new instance
     *
     * @param args the names of the arguments
     * @param st   the function body
     */
    public FirstClassFunction(ArrayList<String> args, Statement st) {
        this.args = args;
        this.st = st;
    }

    /**
     * Evaluates this function
     *
     * @param args the arguments
     * @return the result
     * @throws EvalException EvalException
     */
    public Object evaluate(Object... args) throws EvalException {
        if (args.length != this.args.size())
            throw new EvalException("wrong number of arguments! found: " + args.length + ", expected: " + this.args.size());

        Context c = new Context();
        for (int i = 0; i < args.length; i++)
            c.setVar(this.args.get(i), args[i]);
        st.execute(c);
        return c.getVar("return");
    }

    /**
     * Calls a first class function
     *
     * @param context the context
     * @param args    the arguments
     * @return the result
     * @throws EvalException EvalException
     */
    public Object calcValue(Context context, ArrayList<Expression> args) throws EvalException {
        Object[] data = new Object[args.size()];
        for (int i = 0; i < args.size(); i++)
            data[i] = args.get(i).value(context);

        return evaluate(data);
    }
}
