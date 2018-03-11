/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Simple integer expression
 * Created by Helmut.Neemann on 02.12.2016.
 */
public interface Expression {
    /**
     * calculates the value
     *
     * @param c the context of the calculation
     * @return the long value result
     * @throws ParserException ParserException
     */
    long value(Context c) throws ParserException;
}
