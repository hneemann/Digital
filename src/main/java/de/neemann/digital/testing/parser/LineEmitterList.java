package de.neemann.digital.testing.parser;

import java.util.ArrayList;

/**
 * Created by hneemann on 19.04.17.
 */
public class LineEmitterList implements LineEmitter {

    private ArrayList<LineEmitter> lines;

    /**
     * Create a new instance
     */
    public LineEmitterList() {
        lines = new ArrayList<>();
    }

    /**
     * Adds a line to this list
     *
     * @param line the line to add
     */
    public void add(LineEmitter line) {
        lines.add(line);
    }

    /**
     * If this list contains anly a single line this line is returned.
     * Otherwise this is returned
     *
     * @return this of the only lists item
     */
    public LineEmitter minimize() {
        if (lines.size() == 1)
            return lines.get(0);
        else
            return this;
    }

    @Override
    public void emitLines(LineListener listener, Context conext) throws ParserException {
        for (LineEmitter l : lines)
            l.emitLines(listener, conext);
    }

}
