package de.neemann.digital.integration;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.components.test.TestCaseElement;
import de.neemann.digital.gui.components.test.TestData;
import de.neemann.digital.gui.components.test.TestResult;
import junit.framework.TestCase;

import java.io.File;

/**
 * Reads all testable examples and runs the tests.
 *
 * @author hneemann
 */
public class TestTestableExamples extends TestCase {
    private static final File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig/test");

    public void testTestable() throws Exception {
        assertEquals(4, new FileScanner(this::check).scan(examples));
    }

    /**
     * Loads the model and initializes and tests it
     *
     * @param dig the model file
     */
    private void check(File dig) throws Exception {
        ToBreakRunner br = new ToBreakRunner(dig);

        for (VisualElement el : br.getCircuit().getElements())
            if (el.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)) {

                String label = el.getElementAttributes().getCleanLabel();
                TestData td = el.getElementAttributes().get(TestCaseElement.TESTDATA);

                TestResult tr = new TestResult(td).create(br.getModel());

                if (label.contains("Failing"))
                    assertFalse(dig.getName() + ":" + label, tr.allPassed());
                else
                    assertTrue(dig.getName() + ":" + label, tr.allPassed());
            }
    }
}
