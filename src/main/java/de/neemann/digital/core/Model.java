package de.neemann.digital.core;

import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Reset;
import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * The Model contains all the nodes of the model.
 * Every time the circuit is started a new model is created.
 * The model has the possibility to be run in full step mode (all changes of values are propagated to a stable state)
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
 * But the initial state of the model is undefined. To bring the model to a defined initial state you can use the
 * Reset element. Its output is hold down at zero during startup, and when a stable state is reached it becomes one.
 * </li>
 * </ol>
 * There are also some lists to store special elements. These lists are populated by the elements during the
 * call of the registerNodes method. These lists are necessary to keep track of all elements which are not a node like
 * inputs and outputs. All elements which are nodes can be obtained by {@link #findNode(Class, NodeFilter)} or
 * {@link #findNode(Class)}.
 *
 * @author hneemann
 * @see de.neemann.digital.core.element.Element#registerNodes(Model)
 */
public class Model implements Iterable<Node> {
    /**
     * Maximal number of calculation loops before oscillating behaviour is detected
     */
    private static final int MAX_COUNTER = 1000;

    private final ArrayList<Clock> clocks;
    private final ArrayList<Break> breaks;
    private final ArrayList<Reset> resets;

    private final ArrayList<Signal> signals;
    private final ArrayList<Signal> inputs;
    private final ArrayList<Signal> outputs;

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
        this.outputs = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.nodesToUpdateAct = new ArrayList<>();
        this.nodesToUpdateNext = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Returns the actual step counter.
     * This counter is incremented by every micro step
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
     * Resets and initializes the model.
     * Calls <code>init(true);</code>
     *
     * @throws NodeException NodeException
     */
    public void init() throws NodeException {
        init(true);
    }

    /**
     * Needs to be called after all nodes are added.
     * Resets and initializes the model.
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
     * Performs a step without noise.
     *
     * @throws NodeException NodeException
     */
    public void doStep() throws NodeException {
        doStep(false);
    }

    /**
     * Performs a step.
     * This means all nodes which needs a update are updated, and all further nodes to
     * update are also updated until there is no further node to update.
     * So this method propagates a value change through the whole model.
     *
     * @param noise calculation is performed using noise
     * @throws NodeException NodeException
     */
    public void doStep(boolean noise) throws NodeException {
        int counter = 0;
        if (needsUpdate()) {
            while (needsUpdate()) {
                if (counter++ > MAX_COUNTER) {
                    throw new NodeException(Lang.get("err_seemsToOscillate")).addNodes(nodesToUpdateNext);
                }
                doMicroStep(noise);
            }
        } else
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
     * @param noise if true the micro step is performed with noise
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

        if (nodesToUpdateNext.isEmpty())
            fireEvent(ModelEvent.STEP);
    }

    /**
     * Runs the model until a positive edge at the break element is detected.
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
        fireEvent(ModelEvent.FASTRUN);
        for (int i = 0; i < count; i++) {
            clkVal.setBool(!clkVal.getBool());
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
     * Adds an observer to this model.
     *
     * @param observer the observer to add
     */
    public void addObserver(ModelStateObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer to this model.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(ModelStateObserver observer) {
        observers.remove(observer);
    }

    /**
     * Returns the first observer of the given class.
     *
     * @param observerClass the observer class
     * @param <T>           the type of the observer
     * @return the found observer or null if not present
     */
    public <T extends ModelStateObserver> T getObserver(Class<T> observerClass) {
        for (ModelStateObserver mso : observers)
            if (mso.getClass() == observerClass)
                return (T) mso;
        return null;
    }

    private void fireEvent(ModelEvent event) {
        for (ModelStateObserver observer : observers)
            observer.handleEvent(event);
    }

    /**
     * Registers a Clock to the model.
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
     * Registers a Break to the model
     *
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
     * Registers a Reset to the model.
     *
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
     * Registers a signal to the model.
     *
     * @param signal the signal
     */
    public void addSignal(Signal signal) {
        if (signal.isValid())
            signals.add(signal);
    }

    /**
     * Registers an input to the model.
     *
     * @param signal the signal
     */
    public void addInput(Signal signal) {
        if (signal.isValid()) {
            signals.add(signal);
            inputs.add(signal);
        }
    }

    /**
     * @return the models inputs
     */
    public ArrayList<Signal> getInputs() {
        return inputs;
    }

    /**
     * Registers a output to the model.
     *
     * @param signal the signal
     */
    public void addOutput(Signal signal) {
        if (signal.isValid()) {
            signals.add(signal);
            outputs.add(signal);
        }
    }

    /**
     * @return the models outputs
     */
    public ArrayList<Signal> getOutputs() {
        return outputs;
    }

    /**
     * @return all registered signals
     */
    public ArrayList<Signal> getSignals() {
        return signals;
    }

    /**
     * @return a copy of all registered signals
     */
    public ArrayList<Signal> getSignalsCopy() {
        ArrayList<Signal> n = new ArrayList<>(signals.size());
        n.addAll(signals);
        return n;
    }

    /**
     * Fires a model changed event to all listeners.
     */
    public void fireManualChangeEvent() {
        fireEvent(ModelEvent.MANUALCHANGE);
    }

    /**
     * @return the number of nodes
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Returns all nodes of the given class.
     *
     * @param nodeClass the class
     * @param <NODE>    the node type
     * @return the list, not null, but maybe empty
     */
    public <NODE extends Node> List<NODE> findNode(Class<NODE> nodeClass) {
        return findNode(nodeClass, n -> true);
    }

    /**
     * Returns all nodes of the given class.
     * A filter can be used to narrow down the amount of nodes found.
     *
     * @param nodeClass the class
     * @param filter    filter to filter the nodes
     * @param <NODE>    the node type
     * @return the list, not null, but maybe empty
     */
    public <NODE extends Node> List<NODE> findNode(Class<NODE> nodeClass, NodeFilter<NODE> filter) {
        ArrayList<NODE> found = new ArrayList<>();
        for (Node n : nodes)
            if (n.getClass() == nodeClass && filter.accept((NODE) n))
                found.add((NODE) n);
        return found;
    }

    /**
     * Returns all nodes witch match the given filter.
     *
     * @param filter filter to filter the nodes
     * @return the list, not null, but maybe empty
     */
    public List<Node> findNode(NodeFilter<Node> filter) {
        ArrayList<Node> found = new ArrayList<>();
        for (Node n : nodes)
            if (filter.accept(n))
                found.add(n);
        return found;
    }

    /**
     * A filter for nodes.
     *
     * @param <NODE>
     */
    public interface NodeFilter<NODE extends Node> {
        /**
         * Accepts the node
         *
         * @param n the node
         * @return true if accepted
         */
        boolean accept(NODE n);
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }

    /**
     * Removes a node from this model.
     *
     * @param node the node to remove
     */
    public void removeNode(Node node) {
        nodes.remove(node);
    }

    /**
     * Returns the input with the given name.
     *
     * @param name the name
     * @return the input value
     */
    public ObservableValue getInput(String name) {
        for (Signal i : inputs)
            if (i.getName().equals(name))
                return i.getValue();
        return null;
    }

    /**
     * Returns the output with the given name.
     *
     * @param name the name
     * @return the input value
     */
    public ObservableValue getOutput(String name) {
        for (Signal i : outputs)
            if (i.getName().equals(name))
                return i.getValue();
        return null;
    }
}
