/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.data.Value;

import java.util.ArrayList;

/**
 * Line emitter to create a simple row of values.
 */
public class LineEmitterSimple implements LineEmitter {

    private final ArrayList<ValueAppender> appender;
    private final int valuesCount;
    private final int line;

    /**
     * Creates a new Instance
     *
     * @param valuesCount number of expected columns
     * @param line        the source line
     */
    public LineEmitterSimple(int valuesCount, int line) {
        this.valuesCount = valuesCount;
        this.line = line;
        this.appender = new ArrayList<>();
    }

    /**
     * Adds a value appender
     *
     * @param app the appender
     */
    public void add(ValueAppender app) {
        appender.add(app);
    }

    @Override
    public void emitLines(LineListener listener, Context context) throws ParserException {
        ArrayList<Value> vals = new ArrayList<>(valuesCount);
        for (ValueAppender ve : appender)
            ve.appendValues(vals, context);

        if (vals.size() != valuesCount)
            throw new ParserException(Lang.get("err_testDataExpected_N0_found_N1_numbersInLine_N2", valuesCount, vals.size(), line));

        String description = "L" + line;
        String conString = context.toString();
        if (!conString.isEmpty())
            description += ";" + conString;

        listener.add(new TestRow(vals.toArray(new Value[vals.size()]), description));
    }
}
