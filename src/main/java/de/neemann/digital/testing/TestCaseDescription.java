/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.*;
import de.neemann.digital.testing.parser.functions.Random;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The test data.
 */
public class TestCaseDescription {
    private final String dataString;
    private transient LineEmitter lines;
    private transient ArrayList<String> names;
    private transient ArrayList<VirtualSignal> virtualSignals;
    private transient ModelInitializer modelInitializer;
    private transient Random random;
    private transient long seed;


    /**
     * creates a new instance
     */
    public TestCaseDescription() {
        this.dataString = "";
    }

    /**
     * creates a new instance
     *
     * @param data the test case description
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public TestCaseDescription(String data) throws IOException, ParserException {
        this.dataString = data;
        parseDataString();
    }

    /**
     * creates a new instance
     *
     * @param valueToCopy the instance to copy
     */
    public TestCaseDescription(TestCaseDescription valueToCopy) {
        this.dataString = valueToCopy.dataString;
        this.seed = valueToCopy.seed;
    }

    /**
     * Creates a new seed value
     */
    public void setNewSeed() {
        seed = 0;
    }

    /**
     * @return the data string
     */
    public String getDataString() {
        return dataString;
    }

    private void check() throws TestingDataException {
        if (lines == null || names == null) {
            try {
                parseDataString();
            } catch (ParserException | IOException e) {
                throw new TestingDataException(Lang.get("err_errorParsingTestdata"), e);
            }
        }
        if (seed == 0)
            seed = System.currentTimeMillis();
        random.setSeed(seed);
    }

    /**
     * Resets the seed value used by the random function in test code.
     */
    public void resetSeed() {
        random.setSeed(seed);
    }

    private void parseDataString() throws IOException, ParserException {
        Parser tdp = new Parser(dataString).parse();
        lines = tdp.getLines();
        names = tdp.getNames();
        virtualSignals = tdp.getVirtualSignals();
        modelInitializer = tdp.getModelInitializer();
        random = tdp.getRandom();
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

    /**
     * @return the list of declared virtual signals
     * @throws TestingDataException TestingDataException
     */
    public ArrayList<VirtualSignal> getVirtualSignals() throws TestingDataException {
        check();
        return virtualSignals;
    }

    /**
     * @return the model initializer
     * @throws TestingDataException TestingDataException
     */
    public ModelInitializer getModelInitializer() throws TestingDataException {
        check();
        return modelInitializer;
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

    @Override
    public String toString() {
        return dataString;
    }
}
