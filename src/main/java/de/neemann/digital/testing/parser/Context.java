/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;

/**
 * The context of the calculations.
 * <p>
 */
public class Context {

    /**
     * Returnes the value of a variable
     *
     * @param name the name of the variable
     * @return the value
     * @throws ParserException if the variable does not exist
     */
    public long getVar(String name) throws ParserException {
        throw new ParserException(Lang.get("err_variable_N0_notFound", name));
    }

}
