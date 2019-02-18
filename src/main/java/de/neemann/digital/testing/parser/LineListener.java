/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Listener for truth table lines
 */
public interface LineListener {
    /**
     * Adds a line to the table
     *
     * @param values the values in the line
     */
    void add(TestRow values);
}
