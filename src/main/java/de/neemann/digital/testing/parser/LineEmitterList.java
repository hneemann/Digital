/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import java.util.ArrayList;

/**
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
    public void emitLines(LineListener listener, Context context) throws ParserException {
        for (LineEmitter l : lines)
            l.emitLines(listener, context);
    }

}
