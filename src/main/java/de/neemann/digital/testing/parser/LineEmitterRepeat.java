package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;

/**
 * Repeats some inner table rows.
 * Created by hneemann on 19.04.17.
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
    public void emitLines(LineListener listener, Context conext) throws ParserException {
        ContextWithVar c = new ContextWithVar(conext, name);
        for (int i = 0; i < size; i++) {
            c.setValue(i);
            inner.emitLines(listener, c);
        }
    }
}
