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
 * <br>
 * There are tho ways of model execution: With noise turned on or off.
 * <ol>
 * <li>
 * If noise is turned off, the steps of the model are calculated in a synchronized way. So the change of a signal
 * happens at the same time at every node in the model. And every node in the model needs the same time to update its
 * outputs. In this mode you can observe oscillations in the model, which makes it impossible to start the model.
 * So a RS-FF typically does not start because of oscillations.
 * </li>
 * <li>
 * If noise is turned on, all the nodes to update a updated in a random order. So the startup of a RS-FF is no problem.
 * But the initial state of the model is undefined. To bring the model a defined initial state you can use the
 * Reset element. Its output is hold at zero during startup, and when a stable state is reached it becomes one.
 * </li>
 * </ol>
 * There are also some lists to store special elements. These lists are populated by the elements during the
 * call of the registerNodes method.
 *
 * @author hneemann
 * @see de.neemann.digital.core.element.Element#registerNodes(Model)
 */
public class Model {
    /**
     * Maximal number of calculation loops before oscillating behaviour is detected
     */
    private static final int MAX_COUNTER = 1000;

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
    private boolean isInitialized = false;

    /**
     * Creates a new model
     */
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

    /**
     * Returns the actual step counter
     * This counter is increased by every micro step
     *
     * @return the step counter
     */
    public int getStepCounter() {
        return version;
    }

    /**
     * Adds a node to the model
     *
     * @param node the node
     * @param <T>  type of the node
     * @return the node itself for chained calls
     */
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
     * @throws NodeException NodeException
     */
    public void init() throws NodeException {
        init(true);
    }

    /**
     * Needs to be called after all nodes are added.
     *
     * @param noise setup with ore without noise
     * @throws NodeException NodeException
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

    /**
     * Closes the model.
     * A STOPPED event is fired.
     */
    public void close() {
        fireEvent(ModelEvent.STOPPED);
    }

    /**
     * Adds a node to the update list.
     *
     * @param node the node
     */
    public void addToUpdateList(Node node) {
        nodesToUpdateNext.add(node);
    }

    /**
     * Performes a step without noise.
     *
     * @throws NodeException NodeException
     */
    public void doStep() throws NodeException {
        doStep(false);
    }

    /**
     * Performs a step.
     * This means all Nodes which needs a update are updated, and all further nudes to
     * update are also updated until the is no further Node to update.
     * So this method propagates a value change through the whole model.
     *
     * @param noise calculation is performed using noise
     * @throws NodeException NodeException
     */
    public void doStep(boolean noise) throws NodeException {
        int counter = 0;
        while (needsUpdate()) {
            if (counter++ > MAX_COUNTER) {
                throw new NodeException(Lang.get("err_seemsToOscillate")).addNodes(nodesToUpdateNext);
            }
            doMicroStep(noise);
        }
        fireEvent(ModelEvent.STEP);
    }

    /**
     * Performs a micro step in the model
     * <p>
     * Typical usage is a loop like:
     * <pre>
     * while (needsUpdate())
     *     doMicroStep(noise);
     * </pre>
     *
     * @param noise if true the microstep is performed with noise
     * @throws NodeException NodeException
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
     * Runs the model until a positive edge at the Break element is detected.
     *
     * @return The number of clock cycles necessary to get the positive edge
     * @throws NodeException NodeException
     */
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
                return i + 1;
            }
            lastIn = brIn;
        }
        throw new NodeException(Lang.get("err_breakTimeOut", aBreak.getCycles()), brVal);
    }

    /**
     * @return true if the models allows fast run steps
     */
    public boolean isFastRunModel() {
        return clocks.size() == 1 && breaks.size() == 1;
    }

    /**
     * @return the nodes in this model
     */
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Asks if an update is necessary.
     * <p>
     * Typical usage is a loop like:
     * <pre>
     * while (needsUpdate())
     *     doMicroStep(noise);
     * </pre>
     *
     * @return true if model has more nodes to update
     */
    public boolean needsUpdate() {
        return !nodesToUpdateNext.isEmpty();
    }

    /**
     * @return the nodes to update in the next step
     */
    public Collection<Node> nodesToUpdate() {
        return nodesToUpdateNext;
    }

    /**
     * Adds a observer to this model
     *
     * @param observer the observer to add
     */
    public void addObserver(ModelStateObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes a observer to this model
     *
     * @param observer the observer to remove
     */
    public void removeObserver(ModelStateObserver observer) {
        observers.remove(observer);
    }

    private void fireEvent(ModelEvent event) {
        for (ModelStateObserver observer : observers)
            observer.handleEvent(event);
    }

    /**
     * registers a Clock to the model
     *
     * @param clock the clock
     */
    public void addClock(Clock clock) {
        clocks.add(clock);
    }

    /**
     * @return all registered clocks
     */
    public ArrayList<Clock> getClocks() {
        return clocks;
    }

    /**
     * registers a Break to the model
     * @param aBreak the break
     */
    public void addBreak(Break aBreak) {
        breaks.add(aBreak);
    }

    /**
     * @return all registered Breaks
     */
    public ArrayList<Break> getBreaks() {
        return breaks;
    }

    /**
     * registers a Reset to the model
     * @param reset the Reset
     */
    public void addReset(Reset reset) {
        resets.add(reset);
    }

    /**
     * @return all registered Resets
     */
    public ArrayList<Reset> getResets() {
        return resets;
    }

    /**
     * registers a Signal to the model
     * @param name the signals name
     * @param value the signals value
     */
    public void addSignal(String name, ObservableValue value) {
        if (name != null && name.length() > 0 && value != null)
            signals.add(new Signal(name, value));
    }

    /**
     * @return all registered Signals
     */
    public ArrayList<Signal> getSignals() {
        return signals;
    }

    /**
     * @return a copy of all registered Signals
     */
    public ArrayList<Signal> getSignalsCopy() {
        ArrayList<Signal> n = new ArrayList<>(signals.size());
        n.addAll(signals);
        return n;
    }


    /**
     * registers a ROM to the model
     * @param rom the ROM
     */
    public void addRomListing(ROM rom) {
        roms.add(rom);
    }

    /**
     * @return all registered Roms
     */
    public ArrayList<ROM> getRoms() {
        return roms;
    }

    /**
     * fires a model changed event to all listeners
     */
    public void fireManualChangeEvent() {
        fireEvent(ModelEvent.MANUALCHANGE);
    }

    /**
     * A simple storage bean for signals
     */
    public static final class Signal implements Comparable<Signal> {
        private final String name;
        private final ObservableValue value;

        /**
         * Creates a new Instance
         *
         * @param name  the name of the Signal
         * @param value the signals value
         */
        public Signal(String name, ObservableValue value) {
            this.name = name;
            this.value = value;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the value
         */
        public ObservableValue getValue() {
            return value;
        }

        @Override
        public int compareTo(Signal o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Signal signal = (Signal) o;

            return name.equals(signal.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
