/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Statement;

import java.util.ArrayList;

/**
 * callable first class function
 */
public class FirstClassFunction extends FuncAdapter {
    private final ArrayList<String> args;
    private final Statement st;

    /**
     * Creates a new instance
     *
     * @param args the names of the arguments
     * @param st   the function body
     */
    public FirstClassFunction(ArrayList<String> args, Statement st) {
        super(args.size());
        this.args = args;
        this.st = st;
    }

    /**
     * Evaluates this function
     *
     * @param args the arguments
     * @return the result
     * @throws HGSEvalException HGSEvalException
     */
    @Override
    public Object f(Object... args) throws HGSEvalException {
        Context c = new Context();
        for (int i = 0; i < args.length; i++)
            c.setVar(this.args.get(i), args[i]);
        st.execute(c);
        if (c.contains("return"))
            return c.getVar("return");
        else
            throw new HGSEvalException("A function must define the variable 'return'.");
    }

}
