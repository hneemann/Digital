package de.neemann.digital.testing.parser;

/**
 * Repeats some inner table rows.
 * Created by hneemann on 19.04.17.
 */
public class LineEmitterRepeat implements LineEmitter {

    private final String name;
    private final int size;
    private final LineEmitter inner;

    /**
     * Creates a new loop
     *
     * @param name  name of the loop variable
     * @param size  number of iterations
     * @param inner the lines to repeat
     */
    public LineEmitterRepeat(String name, int size, LineEmitter inner) {
        this.name = name;
        this.size = size;
        this.inner = inner;
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
