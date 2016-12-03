package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.Value;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The context of the calculations.
 * Containf the variables to use and the test values list.
 * Created by hneemann on 02.12.16.
 */
public class Context {
    private final HashMap<String, Long> vars;
    private final ArrayList<Value> values;

    /**
     * Creates a new instance
     */
    public Context() {
        this(null);
    }

    /**
     * Creates a new instance
     *
     * @param values the values array to fill
     */
    public Context(ArrayList<Value> values) {
        vars = new HashMap<>();
        this.values = values;
    }

    /**
     * @return the actual loop value
     * @throws ParserException Thrown if variable not present
     */
    public long getN() throws ParserException {
        return getVar("n");
    }

    /**
     * Returns the value of a variable
     *
     * @param name the variables name
     * @return the long value
     * @throws ParserException Thrown if variable not present
     */
    public long getVar(String name) throws ParserException {
        Long l = vars.get(name);
        if (l == null)
            throw new ParserException(Lang.get("err_variable_N0_notFound", name));
        return l;
    }

    /**
     * Adds a simple value to the value list
     *
     * @param v the value to add
     */
    public void addValue(Value v) {
        values.add(v);
    }

    /**
     * Adds bitcount values to the values list.
     * Bitcount bits from the given value are added to the values list
     *
     * @param bitCount the numbers of bits to add
     * @param value    the bit values
     */
    public void addBits(int bitCount, long value) {
        long mask = 1L << (bitCount - 1);
        for (int i = 0; i < bitCount; i++) {
            boolean v = (value & mask) != 0;
            values.add(new Value(v ? 1 : 0));
            mask >>= 1;
        }
    }

    /**
     * Sets a variable value to the context
     *
     * @param name  name of the variable
     * @param value value
     * @return this for chained calls
     */
    public Context setVar(String name, long value) {
        vars.put(name, value);
        return this;
    }
}
