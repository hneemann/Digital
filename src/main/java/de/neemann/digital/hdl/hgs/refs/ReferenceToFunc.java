/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;
import de.neemann.digital.hdl.hgs.Expression;
import de.neemann.digital.hdl.hgs.function.Function;

import java.util.ArrayList;

/**
 * A reference to a function
 */
public class ReferenceToFunc implements Reference {
    private final Reference parent;
    private final ArrayList<Expression> args;

    /**
     * Creates a new instance
     *
     * @param parent the parent reference
     * @param args   the arguments of the function
     */
    public ReferenceToFunc(Reference parent, ArrayList<Expression> args) {
        this.parent = parent;
        this.args = args;
    }

    @Override
    public void set(Context context, Object value) throws EvalException {
        throw new EvalException("set to function is not possible");
    }

    @Override
    public Object get(Context context) throws EvalException {
        Object funcObj = parent.get(context);
        if (funcObj instanceof Function)
            return ((Function) funcObj).calcValue(context, args);
        throw new EvalException("value is not a function");
    }
}
