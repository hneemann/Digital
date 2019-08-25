/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.LineEmitter;
import de.neemann.digital.testing.parser.Parser;
import de.neemann.digital.testing.parser.ParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The test data.
 */
public class TestCaseDescription {
    private String dataString;
    private transient LineEmitter lines;
    private transient ArrayList<String> names;

    /**
     * creates a new instance
     *
     * @param data the test case description
     */
    public TestCaseDescription(String data) {
        this.dataString = data;
    }

    /**
     * creates a new instance
     *
     * @param valueToCopy the instance to copy
     */
    public TestCaseDescription(TestCaseDescription valueToCopy) {
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

    private void check() throws TestingDataException {
        if (lines == null) {
            try {
                Parser tdp = new Parser(dataString).parse();
                lines = tdp.getLines();
                names = tdp.getNames();
            } catch (ParserException | IOException e) {
                throw new TestingDataException(Lang.get("err_errorParsingTestdata"), e);
            }
        }
    }

    /**
     * @return the data lines
     * @throws TestingDataException TestingDataException
     */
    public LineEmitter getLines() throws TestingDataException {
        check();
        return lines;
    }

    /**
     * @return the signal names
     * @throws TestingDataException TestingDataException
     */
    public ArrayList<String> getNames() throws TestingDataException {
        check();
        return names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCaseDescription testCaseDescription = (TestCaseDescription) o;

        return dataString != null ? dataString.equals(testCaseDescription.dataString) : testCaseDescription.dataString == null;
    }

    @Override
    public int hashCode() {
        return dataString != null ? dataString.hashCode() : 0;
    }
}
