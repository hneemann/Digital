package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestData;
import de.neemann.digital.testing.TestResult;
import junit.framework.TestCase;

import java.io.File;

/**
 * Reads all examples and tries to create the model.
 * Makes sure that all examples are creatable (one can build the model)
 * Does not ensure that they work correctly if no tests are present in the model!
 *
 * @author hneemann
 */
public class TestExamples extends TestCase {

    private int testCasesInFiles = 0;

    /**
     * Tests the examples which are distributed
     *
     * @throws Exception
     */
    public void testDistExamples() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig");
        assertEquals(88, new FileScanner(this::check).scan(examples));
        assertEquals(31, testCasesInFiles);
    }

    /**
     * Tests the examples which are only test cases
     *
     * @throws Exception
     */
    public void testTestExamples() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test");
        assertEquals(27, new FileScanner(this::check).scan(examples));
        assertEquals(19, testCasesInFiles);
    }


    /**
     * Loads the model and initializes and tests it if test cases are present
     *
     * @param dig the model file
     */
    private void check(File dig) throws Exception {
        System.out.println("test " + dig);
        boolean shouldFail = dig.getName().endsWith("Error.dig");
        ToBreakRunner br = null;
        try {
            br = new ToBreakRunner(dig);
            assertFalse("File should fail but doesn't!", shouldFail);
        } catch (Exception e) {
            if (shouldFail)
                return;
            else
                throw e;
        }

        for (VisualElement el : br.getCircuit().getElements())
            if (el.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)) {

                String label = el.getElementAttributes().getCleanLabel();
                TestData td = el.getElementAttributes().get(TestCaseElement.TESTDATA);

                Model model = new ModelCreator(br.getCircuit(), br.getLibrary()).createModel(false);
                TestResult tr = new TestResult(td).create(model);

                if (label.contains("Failing"))
                    assertFalse(dig.getName() + ":" + label, tr.allPassed());
                else
                    assertTrue(dig.getName() + ":" + label, tr.allPassed());

                testCasesInFiles++;
            }
    }
}
