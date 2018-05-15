/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static de.neemann.digital.TestExecuter.IGNORE;

/**
 */
public class TestNesting extends TestCase {

    /**
     * Loads a model with a single nested AND gate.
     * Needs to behave exactly the same as a simple AND gate.
     *
     * @throws Exception
     */
    public void testNestedAnd() throws Exception {
        runTheAndTest("/dig/nestedAnd.dig");
    }

    /**
     * Loads a model with a model which contains a nested AND gate.
     * So it tests nested nesting.
     * And also this needs to be the same as a simple AND gate.
     *
     * @throws Exception
     */
    public void testNestedNestedAnd() throws Exception {
        runTheAndTest("/dig/nestedNestedAnd.dig");
    }

    /**
     * Loads a file and ensures it is a simple and gate.
     *
     * @param file the filename
     * @throws IOException
     * @throws NodeException
     * @throws PinException
     */
    private void runTheAndTest(String file) throws IOException, NodeException, PinException, ElementNotFoundException {
        TestExecuter te = createTestExecuterForNesting(file);
        te.check(0, 0, 0);
        te.check(0, 1, 0);
        te.check(1, 0, 0);
        te.check(1, 1, 1);

        // only a single And-Node
        assertEquals(1, te.getModel().getNodes().size());

        // every calculation needs a single micro step
        assertEquals(4, te.getModel().getStepCounter());
    }

    private TestExecuter createTestExecuterForNesting(String file) throws IOException, NodeException, PinException, ElementNotFoundException {
        ElementLibrary library = new ElementLibrary();
        library.setRootFilePath(new File(Resources.getRoot(), "dig"));
        return TestExecuter.createFromFile(file, library);
    }

    /**
     * Nested JK-FF. One output from nested model is not used!
     *
     * @throws Exception
     */
    public void testMSFF() throws Exception {
        TestExecuter te = createTestExecuterForNesting("dig/nestedMSFF.dig");
        //       C  J  K  Q
        te.checkZ(0, 0, 0, IGNORE);  // initial state is undefined
        te.checkZ(1, 0, 1, IGNORE);
        te.check(0, 0, 0, 0);
        te.check(1, 1, 0, 0);
        te.check(0, 0, 0, 1);
        te.check(1, 1, 1, 1);
        te.check(0, 1, 1, 0);
        te.check(1, 1, 1, 0);
        te.check(0, 1, 1, 1);
    }

    /**
     * Imports the same model two times.
     * A simple traffic light build with two nested MS-JK flip flops.
     */
    public void testMultipleNesting() throws NodeException, PinException, IOException, ElementNotFoundException {
        TestExecuter te = createTestExecuterForNesting("dig/trafficLight.dig");
        te.clockUntil(1, 0, 0);  // runs the sequential logic up to the given state, its necessary because initial state is undefined
        //       C  R  Y  G
        te.check(0, 1, 0, 0);  // Red
        te.check(1, 1, 0, 0);
        te.check(0, 1, 1, 0);  // Red / Yellow
        te.check(1, 1, 1, 0);
        te.check(0, 0, 0, 1);  // Green
        te.check(1, 0, 0, 1);
        te.check(0, 0, 1, 0);  // Yellow
        te.check(1, 0, 1, 0);
        te.check(0, 1, 0, 0);  // Red
        te.check(1, 1, 0, 0);
        te.check(0, 1, 1, 0);  // Red / Yellow
        te.check(1, 1, 1, 0);
        te.check(0, 0, 0, 1);  // Green
        te.check(1, 0, 0, 1);
        te.check(0, 0, 1, 0);  // Yellow
        te.check(1, 0, 1, 0);
        te.check(0, 1, 0, 0);  // Red
        te.check(1, 1, 0, 0);
    }
}
