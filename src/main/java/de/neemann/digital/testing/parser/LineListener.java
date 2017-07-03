package de.neemann.digital.testing.parser;

import de.neemann.digital.data.Value;

/**
 * Listener for truth table lines
 * Created by hneemann on 19.04.17.
 */
public interface LineListener {
    /**
     * Adds a line to the table
     *
     * @param values the values in the line
     */
    void add(Value[] values);
}
