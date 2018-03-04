/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import java.io.File;

/**
 * Implements a node.
 * A node represents a part in the circuit which has a non zero propagation time.
 * So every gate inherits from node.
 * <br/>
 * During the simulation the method {@link #readInputs()} is called.
 * This method has to read all necessary inputs to perform the operation, but is not allowed to update one
 * of the nodes outputs. All necessary data to do so has to be stored in member variables.
 * After all {@link #readInputs()} methods of all nodes are called, the model executer {@link Model#doMicroStep(boolean)}
 * starts to call the {@link #writeOutputs()} methods of all nodes.
 * During this call the outputs have to be updated using the data stored and without reading the inputs again.
 */
public abstract class Node implements NodeInterface {

    private final boolean hasState;
    private Model model;
    private int version;
    // used to store the origin of this node
    // only used to create better error messages
    private File origin;

    /**
     * Creates new stateless Node
     */
    public Node() {
        this(false);
    }

    /**
     * Creates a new node
     *
     * @param hasState true if node has a state
     */
    public Node(boolean hasState) {
        this.hasState = hasState;
    }

    /**
     * Sets the model for this node.
     *
     * @param model the model
     */
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    final public void hasChanged() {
        if (model == null)
            throw new RuntimeException("noModelSet");

        if (model.getStepCounter() != version) {
            model.addToUpdateList(this);
            version = model.getStepCounter();
        }
    }

    /**
     * Only read the input!
     * It is not allowed to write to one of the outputs!!!
     *
     * @throws NodeException NodeException
     */
    public abstract void readInputs() throws NodeException;

    /**
     * Only write to the output!
     * It is not allowed to read from one of the inputs!!!
     *
     * @throws NodeException NodeException
     */
    public abstract void writeOutputs() throws NodeException;

    /**
     * Is called to register all the nodes belonging to this node to the model.
     * this implementation simply registers itself to the model.
     *
     * @param model the model
     */
    public void registerNodes(Model model) {
        model.add(this);
    }

    /**
     * @return true if the element has a state and is not only combinatorial
     */
    public boolean hasState() {
        return hasState;
    }

    /**
     * Returns the origin of this node
     * Only used to show better error messages.
     *
     * @return the origin of this node
     */
    public File getOrigin() {
        return origin;
    }

    /**
     * Sets the origin of this node
     * Only used to show better error messages.
     *
     * @param origin the origin of this node
     */
    public void setOrigin(File origin) {
        this.origin = origin;
    }

    /**
     * @return the model this node belongs to
     */
    public Model getModel() {
        return model;
    }
}
