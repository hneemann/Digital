package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.LibrarySelector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author hneemann
 */
public class ToBreakRunner {

    private final Model model;
    private final Circuit circuit;

    public ToBreakRunner(String file) throws IOException, PinException, NodeException {
        File filename = new File(Resources.getRoot(), file);
        ElementLibrary library = new ElementLibrary();
        ShapeFactory shapeFactory = new ShapeFactory(library);
        circuit = Circuit.loadCircuit(filename, shapeFactory);
        LibrarySelector librarySelector = new LibrarySelector(library, shapeFactory, null);
        librarySelector.setFilePath(filename.getParentFile());

        ModelDescription md = new ModelDescription(circuit, library);
        model = md.createModel();
        model.init(true);

        assertTrue(model.isFastRunModel());
    }

    public ToBreakRunner runToBreak(int steps) throws NodeException {
        assertEquals(steps, model.runToBreak());
        return this;
    }

    public <T extends Node> T getSingleNode(Class<T> clazz) {
        List<T> nodes = model.findNode(clazz);
        assertEquals(1, nodes.size());
        return nodes.get(0);
    }

    public Model getModel() {
        return model;
    }

    public Circuit getCircuit() {
        return circuit;
    }
}
