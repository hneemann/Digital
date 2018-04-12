/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * Represents a statement.
 */
public interface Statement {

    /**
     * Executes the statement
     *
     * @param context the context of the execution
     * @throws HGSEvalException HGSEvalException
     */
    void execute(Context context) throws HGSEvalException;
}
