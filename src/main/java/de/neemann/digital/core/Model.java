package de.neemann.digital.core;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author hneemann
 */
public class Model {

    private final ArrayList<Node> nodes;
    private ArrayList<Node> nodesToUpdateAct;
    private ArrayList<Node> nodesToUpdateNext;
    private int version;
    private int maxCounter = 1000;
    private boolean isInitialized = false;

    public Model() {
        this.nodes = new ArrayList<>();
        this.nodesToUpdateAct = new ArrayList<>();
        this.nodesToUpdateNext = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public <T extends Node> T add(T node) {
        if (isInitialized)
            throw new RuntimeException("is already initialized!");

        nodes.add(node);
        node.setModel(this);
        return node;
    }

    /**
     * Needs to be called after all nodes are added.
     * If not called it es called automatically.
     * Calles <code>init(true);</code>
     *
     * @throws NodeException
     */
    public void init() throws NodeException {
        init(true);
    }

    /**
     * Needs to be called after all nodes are added.
     *
     * @param noise setup with ore without noise
     * @throws NodeException
     */
    public void init(boolean noise) throws NodeException {
        nodesToUpdateNext.addAll(nodes);
        isInitialized = true;
        doStep(noise);
    }


    public void addToUpdateList(Node node) {
        nodesToUpdateNext.add(node);
    }

    public void doStep() throws NodeException {
        doStep(false);
    }

    public void doStep(boolean noise) throws NodeException {
        int counter = 0;
        while (needsUpdate()) {
            doMicroStep(noise);
            if (counter++ > maxCounter) {
                throw new NodeException("seemsToOscillate");
            }
        }
    }

    /**
     * Performs a micro step in the model
     * <p/>
     * Typical usage is a loop like:
     * <pre>
     * while (needsUpdate())
     *     doMicroStep(noise);
     * </pre>
     *
     * @param noise
     * @throws NodeException
     */
    public void doMicroStep(boolean noise) throws NodeException {
        if (!isInitialized)
            throw new RuntimeException("notInitialized!");

        version++;
        // swap lists
        ArrayList<Node> nl = nodesToUpdateNext;
        nodesToUpdateNext = nodesToUpdateAct;
        nodesToUpdateAct = nl;

        nodesToUpdateNext.clear();

        if (noise) {
            Collections.shuffle(nodesToUpdateAct);
            for (Node n : nodesToUpdateAct) {
                n.readInputs();
                n.writeOutputs();
            }
        } else {
            for (Node n : nodesToUpdateAct) {
                n.readInputs();
            }
            for (Node n : nodesToUpdateAct) {
                n.writeOutputs();
            }
        }
    }

    /**
     * Asks if an update is necessary.
     * <p/>
     * Typical usage is a loop like:
     * <pre>
     * while (needsUpdate())
     *     doMicroStep(noise);
     * </pre>
     */
    public boolean needsUpdate() {
        return !nodesToUpdateNext.isEmpty();
    }

}
