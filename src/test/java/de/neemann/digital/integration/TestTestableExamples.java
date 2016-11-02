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
 * Reads all testable examples and runs the tests.
 *
 * @author hneemann
 */
public class TestTestableExamples extends TestCase {

    public void testTestableDist() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig/test");
        assertEquals(9, new FileScanner(this::check).scan(examples));
    }

    public void testTestableTest() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test");
        assertEquals(3, new FileScanner(this::check).scan(examples));
    }

//    public void testFile() throws Exception {
//        check(new File(Resources.getRoot(), "/dig/test/highz.dig"));
//    }

    /**
     * Loads the model and initializes and tests it
     *
     * @param dig the model file
     */
    private void check(File dig) throws Exception {
        System.out.println("test "+dig);
        ToBreakRunner br = new ToBreakRunner(dig);

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
            }
    }
}
