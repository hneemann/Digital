package de.neemann.digital.integration;

import junit.framework.TestCase;

import java.io.File;

/**
 * Reads all examples and tries to create the model.
 * Makes sure that all examples are creatable (one can build the model)
 * Does not ensure that they work correctly!
 *
 * @author hneemann
 */
public class TestExamples extends TestCase {
    private static final File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig");

    public void testCombinatorial() throws Exception {
        assertEquals(28, new FileScanner(this::check).scan(new File(examples, "combinatorial")));
    }

    public void testSequential() throws Exception {
        assertEquals(22, new FileScanner(this::check).scan(new File(examples, "sequential")));
    }

    public void testProcessor() throws Exception {
        assertEquals(19, new FileScanner(this::check).scan(new File(examples, "processor")));
    }

    public void testHazard() throws Exception {
        assertEquals(4, new FileScanner(this::check).scan(new File(examples, "hazard")));
    }

    /**
     * Loads the model and initializes it
     *
     * @param dig the model file
     */
    private void check(File dig) throws Exception {
        new ToBreakRunner(dig);
        assertTrue(true);
    }
}
