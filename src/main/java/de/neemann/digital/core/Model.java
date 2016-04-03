package de.neemann.digital.core;

import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Reset;
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
    private final ArrayList<Reset> resets;
    private final ArrayList<Signal> signals;
    private final ArrayList<ROM> roms;

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
        this.resets = new ArrayList<>();
        this.signals = new ArrayList<>();
        this.roms = new ArrayList<>();
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
        if (!resets.isEmpty()) {
            for (Reset reset : resets)
                reset.getResetOutput().setValue(1);
            doStep(false);
        }
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
        fireEvent(ModelEvent.STEP);
    }

    /**
     * Performs a micro step in the model
     *
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
        fireEvent(ModelEvent.MICROSTEP);
    }

    /**
     * Asks if an update is necessary.
     *
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

    public void addReset(Reset reset) {
        resets.add(reset);
    }

    public void addSignal(String name, ObservableValue value) {
        if (name != null && name.length() > 0 && value != null)
            signals.add(new Signal(name, value));
    }

    public int runToBreak() throws NodeException {
        Break aBreak = breaks.get(0);
        ObservableValue brVal = aBreak.getBreakInput();
        ObservableValue clkVal = clocks.get(0).getClockOutput();

        int count = aBreak.getCycles() * 2;
        boolean lastIn = brVal.getBool();
        for (int i = 0; i < count; i++) {
            clkVal.setValue(clkVal.getBool() ? 0 : 1);
            doStep();
            boolean brIn = brVal.getBool();
            if (!lastIn && brIn) {
                fireEvent(ModelEvent.BREAK);
                return i;
            }
            lastIn = brIn;
        }
        throw new NodeException(Lang.get("err_breakTimeOut", aBreak.getCycles()), null, brVal);
    }

    public boolean isFastRunModel() {
        return clocks.size() == 1 && breaks.size() == 1;
    }

    public ArrayList<Signal> getSignals() {
        return signals;
    }

    public void addRomListing(ROM rom) {
        roms.add(rom);
    }

    public ArrayList<ROM> getRoms() {
        return roms;
    }

    public static class Signal implements Comparable<Signal> {

        private final String name;
        private final ObservableValue value;

        public Signal(String name, ObservableValue value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public ObservableValue getValue() {
            return value;
        }

        @Override
        public int compareTo(Signal o) {
            return name.compareTo(o.name);
        }
    }

}
