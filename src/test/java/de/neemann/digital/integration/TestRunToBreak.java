package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * @author hneemann
 */
public class TestRunToBreak extends TestCase {
    private ElementLibrary library = new ElementLibrary();

    public void testRunToBreak() throws IOException, NodeException, PinException {
        File filename = new File(Resources.getRoot(), "dig/runToBreak.dig");
        Circuit circuit = Circuit.loadCircuit(filename, new ShapeFactory(new ElementLibrary()));

        ModelDescription md = new ModelDescription(circuit, library);
        Model model = md.createModel();
        model.init(true);

        assertTrue(model.isFastRunModel());
        int halfClocks = model.runToBreak();
        assertEquals(511, halfClocks);
    }

}
