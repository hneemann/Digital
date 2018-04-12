/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * Simple HGS expression
 */
public interface Expression {
    /**
     * Calculates the value
     *
     * @param c the context of the calculation
     * @return the result of the evaluation
     * @throws HGSEvalException HGSEvalException
     */
    Object value(Context c) throws HGSEvalException;
}
