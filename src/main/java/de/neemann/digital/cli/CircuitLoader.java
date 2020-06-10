/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.io.File;
import java.io.IOException;

/**
 * Helper to make it easier to load a circuit.
 */
public final class CircuitLoader {
    private final Circuit circuit;
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;

    /**
     * Loads a circuit.
     *
     * @param filename filename
     * @throws IOException IOException
     */
    public CircuitLoader(String filename) throws IOException {
        this(new File(filename), true);
    }

    /**
     * Loads a circuit.
     *
     * @param filename   filename
     * @param ieeeShapes if true ieee shapes are used
     * @throws IOException IOException
     */
    public CircuitLoader(String filename, boolean ieeeShapes) throws IOException {
        this(new File(filename), ieeeShapes);
    }

    /**
     * Loads a circuit.
     *
     * @param file filename
     * @throws IOException IOException
     */
    public CircuitLoader(File file) throws IOException {
        this(file, true);
    }

    /**
     * Loads a circuit.
     *
     * @param file       filename
     * @param ieeeShapes if true ieee shapes are used
     * @throws IOException IOException
     */
    public CircuitLoader(File file, boolean ieeeShapes) throws IOException {
        library = new ElementLibrary();
        library.setRootFilePath(file.getAbsoluteFile().getParentFile());
        shapeFactory = new ShapeFactory(library, ieeeShapes);
        circuit = Circuit.loadCircuit(file, shapeFactory);
    }

    /**
     * @return the circuit
     */
    public Circuit getCircuit() {
        return circuit;
    }

    /**
     * @return the created library
     */
    public ElementLibrary getLibrary() {
        return library;
    }

    /**
     * @return the created shape factory
     */
    public ShapeFactory getShapeFactory() {
        return shapeFactory;
    }

    /**
     * Creates a mode from the loaded circuit.
     *
     * @return the model
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     */
    public Model createModel() throws ElementNotFoundException, PinException, NodeException {
        return new ModelCreator(circuit, library).createModel(false);
    }
}
