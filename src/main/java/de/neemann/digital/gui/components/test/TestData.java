package de.neemann.digital.gui.components.test;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The test data
 *
 * @author hneemann
 */
public class TestData implements Iterable<int[]> {

    /**
     * the default instance
     */
    public static final TestData DEFAULT = new TestData("");

    private String dataString;
    private transient ArrayList<int[]> lines;
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

    @Override
    public Iterator<int[]> iterator() {
        return lines.iterator();
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
     * @throws DataException thrown if data is not valid
     */
    public void setDataString(String data) throws DataException {
        if (!data.equals(dataString)) {
            TestDataParser tdp = new TestDataParser(data).parse();
            dataString = data;
            lines = tdp.getLines();
            names = tdp.getNames();
        }
    }

    private void check() {
        if (lines == null) {
            try {
                TestDataParser tdp = new TestDataParser(dataString).parse();
                lines = tdp.getLines();
                names = tdp.getNames();
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return the data lines
     */
    public ArrayList<int[]> getLines() {
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
