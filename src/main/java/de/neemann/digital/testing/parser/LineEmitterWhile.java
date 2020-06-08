/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Line emitter which implements the while loop
 */
public class LineEmitterWhile implements LineEmitter {
    private final Expression condition;
    private final LineEmitter inner;

    /**
     * Creates a new instance
     *
     * @param condition the condition
     * @param inner     the inner LineEmitter
     */
    public LineEmitterWhile(Expression condition, LineEmitter inner) {
        this.condition = condition;
        this.inner = inner;
    }

    @Override
    public void emitLines(LineListener listener, Context context) throws ParserException {
        while (condition.value(context) != 0)
            inner.emitLines(listener, context);
    }
}
