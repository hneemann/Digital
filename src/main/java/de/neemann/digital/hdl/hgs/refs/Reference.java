/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;

/**
 * A reference to a value
 */
public interface Reference {

    /**
     * Declares a new variable
     *
     * @param context the context of the operation
     * @param initial the initial value
     * @throws HGSEvalException HGSEvalException
     */
    default void exportVar(Context context, Object initial) throws HGSEvalException {
        throw new HGSEvalException("export not allowed here!");
    }

    /**
     * Declares a new variable
     *
     * @param context the context of the operation
     * @param initial the initial value
     * @throws HGSEvalException HGSEvalException
     */
    void declareVar(Context context, Object initial) throws HGSEvalException;

    /**
     * Sets a value
     *
     * @param context the context of the operation
     * @param value   the value to set
     * @throws HGSEvalException HGSEvalException
     */
    void set(Context context, Object value) throws HGSEvalException;

    /**
     * Returns the value
     *
     * @param context the context of the operation
     * @return the value
     * @throws HGSEvalException HGSEvalException
     */
    Object get(Context context) throws HGSEvalException;
}
