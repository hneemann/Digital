package de.neemann.digital.core;

import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The Model contains all the nodes of the model.
 * It has also the possibility to run the model in full step mode (all changes of values are propagated to a stable state)
 * or in micro stepping mode: Only the gates which had a change on one of the input signals are updated. Then the
 * calculation is stopped.
 *
 * @author hneemann
 */
public class Model {

    private final ArrayList<Clock> clocks;
    private final ArrayList<Break> breaks;
    private final ArrayList<Signal> signals;

    private final ArrayList<Node> nodes;
    private final ArrayList<ModelStateObserver> observers;
    private ArrayList<Node> nodesToUpdateAct;
    private ArrayList<Node> nodesToUpdateNext;
    private int version;
    private int maxCounter = 1000;
    private boolean isInitialized = false;

    public Model() {
        this.clocks = new ArrayList<>();
        this.breaks = new ArrayList<>();
        this.signals = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.nodesToUpdateAct = new ArrayList<>();
        this.nodesToUpdateNext = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public int getStepCounter() {
        return version;
    }

    public <T extends Node> T add(T node) {
        if (isInitialized)
            throw new RuntimeException(Lang.get("err_isAlreadyInitialized"));

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
        fireEvent(ModelEvent.STARTED);
    }

    public void close() {
        fireEvent(ModelEvent.STOPPED);
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
            if (counter++ > maxCounter) {
                throw new NodeException(Lang.get("err_seemsToOscillate"), nodesToUpdateNext.get(0));
            }
            doMicroStep(noise);
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
            throw new RuntimeException(Lang.get("err_notInitialized"));

        fireEvent(ModelEvent.STEP);

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

    public Collection<Node> nodesToUpdate() {
        return nodesToUpdateNext;
    }

    public void addObserver(ModelStateObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ModelStateObserver observer) {
        observers.remove(observer);
    }

    private void fireEvent(ModelEvent event) {
        for (ModelStateObserver observer : observers)
            observer.handleEvent(event);
    }

    public ArrayList<Clock> getClocks() {
        return clocks;
    }

    public ArrayList<Break> getBreaks() {
        return breaks;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void addClock(Clock clock) {
        clocks.add(clock);
    }

    public void addBreak(Break aBreak) {
        breaks.add(aBreak);
    }

    public void addSignal(String name, ObservableValue value) {
        if (name != null && name.length() > 0) {
            signals.add(new Signal(name, value));
            System.out.println(name + ": " + value);
        }
    }

    public int runToBreak(Clock clock, Break br) throws NodeException {
        ObservableValue brVal = br.getBreakInput();
        ObservableValue clkVal = clock.getClockOutput();

        int count = br.getCycles() * 2;
        boolean lastIn = brVal.getBool();
        for (int i = 0; i < count; i++) {
            clkVal.setValue(clkVal.getBool() ? 0 : 1);
            doStep();
            boolean brIn = brVal.getBool();
            if (!lastIn && brIn) {
                return i;
            }
            lastIn = brIn;
        }
        throw new NodeException(Lang.get("err_breakTimeOut"), null, brVal);
    }

    public static class Signal {

        private final String name;
        private final ObservableValue value;

        public Signal(String name, ObservableValue value) {
            this.name = name;
            this.value = value;
        }
    }

}
