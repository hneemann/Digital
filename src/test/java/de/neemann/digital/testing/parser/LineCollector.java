/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.data.Value;

import java.util.ArrayList;

/**
 *
 */
public class LineCollector implements LineListener {
    private final ArrayList<String> names;
    private final ArrayList<TestRow> list;

    public LineCollector(LineEmitter le) throws ParserException {
        this.list = new ArrayList<>();
        le.emitLines(this, new Context());
        names = null;
    }

    public LineCollector(Parser parser) throws ParserException {
        this(parser, new Context());
    }

    public LineCollector(Parser parser, Context context) throws ParserException {
        this.list = new ArrayList<>();
        parser.getLines().emitLines(this, context);
        names = parser.getNames();
    }

    @Override
    public void add(TestRow values) {
        list.add(values);
    }

    public ArrayList<TestRow> getLines() {
        return list;
    }

    public ArrayList<String> getNames() {
        return names;
    }
}
