package de.neemann.digital.testing.parser;

/**
 * Used to describe a line
 * Created by hneemann on 19.04.17.
 */
public interface LineEmitter {

    /**
     * Is called to eimit the described line to the listener
     *
     * @param listener the listener to emit the lines
     * @param context  the context
     * @throws ParserException ParserException
     */
    void emitLines(LineListener listener, Context context) throws ParserException;

}
