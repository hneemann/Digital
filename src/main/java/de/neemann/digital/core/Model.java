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
    private Observer observer;

    public Model() {
        this.nodes = new ArrayList<>();
        this.nodesToUpdateAct = new ArrayList<>();
        this.nodesToUpdateNext = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public <T extends Node> T add(T node) {
        nodes.add(node);
        node.setModel(this);
        return node;
    }

    public void addToUpdateList(Node node) {
        nodesToUpdateNext.add(node);
    }

    public void doStep() throws NodeException {
        doStep(false);
    }

    public void doStep(boolean noise) throws NodeException {
        int counter = 0;
        while (!doMicroStep(noise)) {
            if (observer != null)
                observer.hasChanged();
            if (counter++ > maxCounter) {
                throw new NodeException("seemsToOscillate");
            }
        }
    }

    public boolean doMicroStep(boolean noise) throws NodeException {
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
        return nodesToUpdateNext.isEmpty();
    }

    public void init() throws NodeException {
        init(false);
    }

    public void init(boolean noise) throws NodeException {
        nodesToUpdateNext.addAll(nodes);
        doStep(noise);
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }
}
