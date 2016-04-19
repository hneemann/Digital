package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Reads all examples and tries to create the model.
 * Makes sure that all examples are creatable (one can build the model)
 * Does not ensure that they work correctly!
 *
 * @author hneemann
 */
public class TestExamples extends TestCase {

    public void testExamples() throws Exception {
        File res = Resources.getRoot();
        File src = res.getParentFile().getParentFile();
        File examples = new File(src, "/main/dig");
        assertEquals(17, scan(new File(examples, "combinatorial")));
        assertEquals(18, scan(new File(examples, "sequential")));
        assertEquals(16, scan(new File(examples, "processor")));
        assertEquals(4, scan(new File(examples, "hazard")));
    }

    private int scan(File path) {
        int count = 0;
        File[] files = path.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.getName().charAt(0) != '.') {
                        count += scan(f);
                    }
                } else {
                    if (f.getName().endsWith(".dig")) {
                        check(f);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Loads the model and initializes it
     *
     * @param dig the model file
     */
    private void check(File dig) {
        try {
            new ToBreakRunner(dig);
            assertTrue(true);
        } catch (PinException | NodeException | IOException e) {
            assertTrue(dig.getPath() + ": " + e.getMessage(), false);
        }

    }
}
