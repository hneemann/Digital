/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;

/**
 * Repeats some inner table rows.
 */
public class LineEmitterRepeat implements LineEmitter {
    private static final long MAX_LOOPS = 1L << 24;

    private final String name;
    private final int size;
    private final LineEmitter inner;

    /**
     * Creates a new loop
     *
     * @param name  name of the loop variable
     * @param size  number of iterations
     * @param inner the lines to repeat
     * @throws ParserException if there are to many iterations
     */
    public LineEmitterRepeat(String name, long size, LineEmitter inner) throws ParserException {
        this.name = name;
        this.size = (int) size;
        this.inner = inner;

        if (size > MAX_LOOPS)
            throw new ParserException(Lang.get("err_toManyIterations"));
    }

    @Override
    public void emitLines(LineListener listener, Context context) throws ParserException {
        Context c = new Context(context);
        for (int i = 0; i < size; i++) {
            c.setVar(name, i);
            inner.emitLines(listener, c);
        }
    }
}
