/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.ErrorDetector;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;
import junit.framework.TestCase;

import java.io.File;

/**
 * Reads all examples and tries to create the model.
 * Makes sure that all examples are creatable (one can build the model)
 * Does not ensure that they work correctly if no tests are present in the circuit!
 */
public class TestExamples extends TestCase {

    private int testCasesInFiles;

    /**
     * Tests the examples which are distributed
     *
     * @throws Exception Exception
     */
    public void testDistExamples() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig");
        assertEquals(301, new FileScanner(this::check).scan(examples));
        assertEquals(196, testCasesInFiles);
    }

    /**
     * Tests the examples which are only test cases
     *
     * @throws Exception Exception
     */
    public void testTestExamples() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test");
        assertEquals(193, new FileScanner(this::check).scan(examples));
        assertEquals(182, testCasesInFiles);
    }

    /**
     * Loads the model and initializes and test it if test cases are present
     *
     * @param dig the model file
     */
    public void check(File dig) throws Exception {
        boolean shouldFail = dig.getName().endsWith("Error.dig");
        ToBreakRunner br;
        try {
            br = new ToBreakRunner(dig);
        } catch (Exception e) {
            if (shouldFail) {
                return;
            } else
                throw e;
        }
        try {

            boolean isLib = dig.getPath().replace('\\', '/').contains("/lib/");

            assertEquals("wrong locked mode", isLib, (boolean) br.getCircuit().getAttributes().get(Keys.LOCKED_MODE));

            try {
                for (VisualElement el : br.getCircuit().getElements())
                    if (el.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)) {
                        testCasesInFiles++;

                        String label = el.getElementAttributes().getLabel();
                        TestCaseDescription td = el.getElementAttributes().get(TestCaseElement.TESTDATA);

                        Model model = new ModelCreator(br.getCircuit(), br.getLibrary()).createModel(false);
                        ErrorDetector ed = new ErrorDetector();
                        model.addObserver(ed);
                        try {
                            TestExecutor tr = new TestExecutor(td).create(model);

                            if (label.contains("Failing"))
                                assertFalse(dig.getName() + ":" + label, tr.allPassed());
                            else
                                assertTrue(dig.getName() + ":" + label, tr.allPassed());

                        } finally {
                            model.close();
                        }
                        ed.check();
                    }
            } catch (Exception e) {
                if (shouldFail) {
                    return;
                } else
                    throw e;
            }

            assertFalse("File should fail but doesn't!", shouldFail);
        } finally {
            br.close();
        }
    }
}
