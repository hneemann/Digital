/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Used to describe a line
 */
public interface LineEmitter {

    /**
     * Is called to emit the described line to the listener.
     * A emitter is allowed to emit multiple lines to the listener.
     *
     * @param listener the listener to emit the lines
     * @param context  the context
     * @throws ParserException ParserException
     */
    void emitLines(LineListener listener, Context context) throws ParserException;

}
