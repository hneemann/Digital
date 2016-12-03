package de.neemann.digital.testing;

import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.Parser;
import de.neemann.digital.testing.parser.ParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The test data.
 *
 * @author hneemann
 */
public class TestData {

    /**
     * the default instance
     */
    public static final TestData DEFAULT = new TestData("");

    private String dataString;
    private transient ArrayList<Value[]> lines;
    private transient ArrayList<String> names;

    TestData(String data) {
        this.dataString = data;
    }

    /**
     * creates a new instance
     *
     * @param valueToCopy the instance to copy
     */
    public TestData(TestData valueToCopy) {
        this(valueToCopy.dataString);
    }

    /**
     * @return the data string
     */
    public String getDataString() {
        return dataString;
    }

    /**
     * Sets the data and checks its validity
     *
     * @param data the data
     * @throws IOException     thrown if data is not valid
     * @throws ParserException thrown if data is not valid
     */
    public void setDataString(String data) throws IOException, ParserException {
        if (!data.equals(dataString)) {
            Parser tdp = new Parser(data).parse();
            dataString = data;
            lines = tdp.getLines();
            names = tdp.getNames();
        }
    }

    private void check() {
        if (lines == null) {
            try {
                Parser tdp = new Parser(dataString).parse();
                lines = tdp.getLines();
                names = tdp.getNames();
            } catch (ParserException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return the data lines
     */
    public ArrayList<Value[]> getLines() {
        check();
        return lines;
    }

    /**
     * @return the signal names
     */
    public ArrayList<String> getNames() {
        check();
        return names;
    }
}
