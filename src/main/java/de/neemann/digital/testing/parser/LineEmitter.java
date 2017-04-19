package de.neemann.digital.testing.parser;

/**
 * Used to describe a line
 * Created by hneemann on 19.04.17.
 */
public interface LineEmitter {

    /**
     * Is called to imit the described line to the listener
     *
     * @param listener the listener to emit the lines
     * @param conext   the context
     * @throws ParserException ParserException
     */
    void emitLines(LineListener listener, Context conext) throws ParserException;

}
