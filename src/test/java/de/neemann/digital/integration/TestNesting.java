package de.neemann.digital.integration;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.gui.LibrarySelector;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.library.ElementLibrary;
import de.neemann.digital.gui.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * @author hneemann
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
    private void runTheAndTest(String file) throws IOException, NodeException, PinException {
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

    private TestExecuter createTestExecuterForNesting(String file) throws IOException, NodeException, PinException {
        ElementLibrary library = new ElementLibrary();
        LibrarySelector librarySelector = new LibrarySelector(library);
        librarySelector.setFilePath(new File(Resources.getRoot(), "dig"));
        ShapeFactory.getInstance().setLibrary(library);
        return TestExecuter.createFromFile(file, library);
    }

    /**
     * Nested JK-FF. One from tested model is not used!
     *
     * @throws Exception
     */
    public void testMSFF() throws Exception {
        TestExecuter te = createTestExecuterForNesting("dig/nestedMSFF.dig");
        //       C  J  K  Q
        te.check(0, 0, 0, -1);  // initial state is undefined
        te.check(1, 0, 1, -1);
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
     * A simple traffic light build with two nested MS-JK flipflops.
     */
    public void testMultipleNesting() throws NodeException, PinException, IOException {
        TestExecuter te = createTestExecuterForNesting("dig/trafficLight.dig");
        te.clockUntil(1, 0, 0);  // runs the sequential logic up to the given state
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
    }
}
