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
        Object listObj = parent.get(context);
        if (listObj instanceof List) {
            final List list = (List) listObj;
            final int i = Expression.toInt(index.value(context));
            if (i < 0)
                throw new EvalException("index out of bounds: " + i);
            while (list.size() <= i)
                list.add(null);
            list.set(i, value);
        } else
            throw new EvalException("not an array: " + listObj);
    }

    @Override
    public Object get(Context context) throws EvalException {
        Object listObj = parent.get(context);
        if (listObj instanceof List) {
            final List list = (List) listObj;
            final int i = Expression.toInt(index.value(context));
            if (i >= list.size() || i < 0)
                throw new EvalException("index out of bounds: " + i);
            return list.get(i);
        } else
            throw new EvalException("not an array: " + listObj);
    }
}
