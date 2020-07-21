/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.ErrorDetector;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Loads a circuit and runs it to the first break point
 */
public class ToBreakRunner {

    private final Model model;
    private final Circuit circuit;
    private final ElementLibrary library;
    private final ErrorDetector errorDetector;

    /**
     * Creates a new instance
     *
     * @param file the file to load
     */
    public ToBreakRunner(String file) throws Exception {
        this(new File(Resources.getRoot(), file));
    }

    /**
     * Creates a new instance
     *
     * @param file   the file to load
     * @param doInit if true model is initialized
     */
    public ToBreakRunner(String file, boolean doInit) throws Exception {
        this(new File(Resources.getRoot(), file), doInit);
    }

    /**
     * Creates a new instance
     *
     * @param filename the file to load
     */
    public ToBreakRunner(File filename) throws Exception {
        this(filename, true);
    }

    private ToBreakRunner(File filename, boolean doInit) throws Exception {
        library = new ElementLibrary();
        initLibrary(library);
        library.setRootFilePath(filename.getParentFile());
        ShapeFactory shapeFactory = new ShapeFactory(library);
        circuit = Circuit.loadCircuit(filename, shapeFactory);

        ModelCreator md = new ModelCreator(circuit, library);
        model = md.createModel(false);
        errorDetector = new ErrorDetector();
        model.addObserver(errorDetector);
        if (doInit) {
            if (model.getAsyncInfos() != null)
                model.setAsyncMode();
            model.init(true);
        }
        errorDetector.check();
    }

    /**
     * Override this method to prepare the library.
     * This implementation does nothing.
     *
     * @param library the library
     */
    public void initLibrary(ElementLibrary library) {
    }

    /**
     * Runs the model to a break point and checks the needed ticks
     *
     * @param steps the needed ticks
     * @return this for chained calls
     */
    public ToBreakRunner runToBreak(int steps) throws Exception {
        assertTrue(model.isRunToBreakAllowed());
        assertEquals(steps, model.runToBreak().getSteps());
        errorDetector.check();
        return this;
    }

    /**
     * Returns single node of the given class.
     * If there is more than one maching node, an exception is thrown.
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

    /**
     * @return the library
     */
    public ElementLibrary getLibrary() {
        return library;
    }

    public void close() {
        if (model != null)
            model.close();
    }
}
