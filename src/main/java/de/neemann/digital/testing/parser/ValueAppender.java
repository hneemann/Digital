package de.neemann.digital.testing.parser;

import de.neemann.digital.data.Value;

import java.util.ArrayList;

/**
 * Used to append some values to a table row
 * Created by hneemann on 19.04.17.
 */
public interface ValueAppender {
    /**
     * Appends some values to the given row
     *
     * @param values  the row
     * @param context the context to acess variables
     * @throws ParserException ParserException
     */
    void appendValues(ArrayList<Value> values, Context context) throws ParserException;
}
