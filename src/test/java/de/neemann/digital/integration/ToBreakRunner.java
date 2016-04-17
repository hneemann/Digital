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
 * Loads a circuit and runs it to the first break point
 *
 * @author hneemann
 */
public class ToBreakRunner {

    private final Model model;
    private final Circuit circuit;

    /**
     * Creates a new instance
     *
     * @param file the file to load
     * @throws IOException
     * @throws PinException
     * @throws NodeException
     */
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

    /**
     * Runs the model to a break point and checks the needed ticks
     *
     * @param steps the needed ticks
     * @return this for chained calls
     * @throws NodeException
     */
    public ToBreakRunner runToBreak(int steps) throws NodeException {
        assertEquals(steps, model.runToBreak());
        return this;
    }

    /**
     * Returns single node of the given class.
     * If there more then one maching node, an exception is thrown.
     *
     * @param clazz the class
     * @param <T>   the type of the node
     * @return the node instance
     */
    public <T extends Node> T getSingleNode(Class<T> clazz) {
        List<T> nodes = model.findNode(clazz);
        assertEquals(1, nodes.size());
        return nodes.get(0);
    }

    /**
     * @return the mode
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return the circuit
     */
    public Circuit getCircuit() {
        return circuit;
    }
}
