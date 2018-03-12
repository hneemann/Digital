/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;
import de.neemann.digital.hdl.hgs.Expression;

import java.util.List;

/**
 * Handles the access to arrays
 */
public class ReferenceToArray implements Reference {

    private final Reference parent;
    private final Expression index;

    /**
     * Creates a new Array access
     *
     * @param parent the parent reference
     * @param index  the index to access
     */
    public ReferenceToArray(Reference parent, Expression index) {
        this.parent = parent;
        this.index = index;
    }

    @Override
    public void set(Context context, Object value) throws EvalException {
        Object ar = parent.get(context);
        if (ar instanceof List) {
            final List list = (List) ar;
            final int index = Expression.toInt(this.index.value(context));
            while (list.size() <= index)
                list.add(null);
            list.set(index, value);
        } else
            throw new EvalException("not an array: " + ar);
    }

    @Override
    public Object get(Context context) throws EvalException {
        Object ar = parent.get(context);
        if (ar instanceof List)
            return ((List) ar).get(Expression.toInt(index.value(context)));
        else
            throw new EvalException("not an array: " + ar);
    }
}
