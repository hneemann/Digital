/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;

/**
 * A reference to a variable
 */
public interface Reference {
    /**
     * Sets a value
     *
     * @param context the context of the operation
     * @param value   the value to set
     * @throws EvalException EvalException
     */
    void set(Context context, Object value) throws EvalException;

    /**
     * Returns the value
     *
     * @param context the context of the operation
     * @return the value
     * @throws EvalException EvalException
     */
    Object get(Context context) throws EvalException;
}
