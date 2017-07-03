package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.data.Value;

import java.util.ArrayList;

/**
 * Line emitter to create a simple row of values.
 * Created by hneemann on 19.04.17.
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
    public void emitLines(LineListener listener, Context conext) throws ParserException {
        ArrayList<Value> vals = new ArrayList<>(valuesCount);
        for (ValueAppender ve : appender)
            ve.appendValues(vals, conext);

        if (vals.size() != valuesCount)
            throw new ParserException(Lang.get("err_testDataExpected_N0_found_N1_numbersInLine_N2", valuesCount, vals.size(), line));

        listener.add(vals.toArray(new Value[vals.size()]));
    }
}
