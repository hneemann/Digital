/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.core.io.Button;
import de.neemann.digital.core.wiring.AsyncSeq;
import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Reset;
import de.neemann.digital.gui.components.WindowPosManager;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @see de.neemann.digital.core.element.Element#registerNodes(Model)
 */
public class Model implements Iterable<Node>, SyncAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);
    /**
     * Maximal number of calculation loops before oscillating behaviour is detected
     */
    private static final int MAX_LOOP_COUNTER = 1000;
    private static final int COLLECTING_LOOP_COUNTER = MAX_LOOP_COUNTER + 100;

    private enum State {BUILDING, INITIALIZING, RUNNING, CLOSED}

    private State state = State.BUILDING;

    private final ArrayList<Clock> clocks;
    private final ArrayList<Break> breaks;
    private final ArrayList<Reset> resets;
    private final HashMap<Integer, Button> buttonsToMap;

    private final ArrayList<Signal> signals;
    private final ArrayList<Signal> inputs;
    private final ArrayList<Signal> outputs;

    private final ArrayList<Node> nodes;
    private ArrayList<Node> nodesToUpdateAct;
    private ArrayList<Node> nodesToUpdateNext;
    private int version;
    private WindowPosManager windowPosManager;
    private HashSet<Node> oscillatingNodes;
    private Signal invalidSignal = null;
    private AsyncSeq asyncInfos;
    private boolean asyncMode = false;
    private boolean allowGlobalValues = false;

    private final ArrayList<ModelStateObserver> observers;
    private ArrayList<ModelStateObserver> observersStep;
    private ArrayList<ModelStateObserver> observersMicroStep;

    /**
     * Creates a new model
     */
    public Model() {
        this.clocks = new ArrayList<>();
        this.breaks = new ArrayList<>();
        this.resets = new ArrayList<>();
        this.buttonsToMap = new HashMap<>();
        this.signals = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.nodesToUpdateAct = new ArrayList<>();
        this.nodesToUpdateNext = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Sets this model to async mode.
     * Async mode means that the circuit is not able to reach a stable state once the reset gates are released.
     *
     * @return this for chained calls
     */
    public Model setAsyncMode() {
        this.asyncMode = true;
        return this;
    }

    /**
     * Sets the window position manager.
     * Allows the model to place new and close old gui windows.
     *
     * @param windowPosManager the window position manager
     */
    public void setWindowPosManager(WindowPosManager windowPosManager) {
        this.windowPosManager = windowPosManager;
    }

    /**
     * The WindowPosManager allows the model to place new and close old gui windows.
     *
     * @return the window position manager
     */
    public WindowPosManager getWindowPosManager() {
        if (windowPosManager == null)
            windowPosManager = new WindowPosManager(null);
        return windowPosManager;
    }

    /**
     * @return true if this model runs in the main frame
     */
    public boolean runningInMainFrame() {
        if (windowPosManager == null)
            return false;
        return windowPosManager.getMainFrame() != null;
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
        if (state != State.BUILDING)
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
     * @param noise setup with or without noise
     * @throws NodeException NodeException
     */
    public void init(boolean noise) throws NodeException {
        nodesToUpdateNext.addAll(nodes);
        state = State.INITIALIZING;
        doStep(noise);
        // state is CLOSED if an error during the first doStep has occurred!
        if (state != State.CLOSED) {
            if (!resets.isEmpty()) {
                for (Reset reset : resets)
                    reset.clearReset();
                if (!asyncMode)
                    doStep(false);
                else
                    doMicroStep(false);
            }
            LOGGER.debug("stabilizing took " + version + " micro steps");
            state = State.RUNNING;
            fireEvent(ModelEvent.STARTED);
        }
    }

    /**
     * Closes the model.
     * A STOPPED event is fired.
     */
    public void close() {
        if (state != State.CLOSED) {
            state = State.CLOSED;
            int obs = observers.size();
            if (observersStep != null) obs += observersStep.size();
            if (observersMicroStep != null) obs += observersMicroStep.size();
            LOGGER.debug("Observers " + obs);
            for (ModelStateObserver ob : observers)
                LOGGER.debug("Observer Slow : " + ob.getClass().getSimpleName());
            if (observersStep != null)
                for (ModelStateObserver ob : observersStep)
                    LOGGER.debug("Observer Step : " + ob.getClass().getSimpleName());
            if (observersMicroStep != null)
                for (ModelStateObserver ob : observersMicroStep)
                    LOGGER.debug("Observer Micro: " + ob.getClass().getSimpleName());

            fireEvent(ModelEvent.CLOSED);
        }
    }

    /**
     * Called if a error has occurred during model execution.
     * Also closes the model.
     *
     * @param cause the cause
     */
    public void errorOccurred(Exception cause) {
        if (state != State.CLOSED)
            fireEvent(new ModelEvent(cause));
        close();
    }

    /**
     * @return true if model is not closed
     */
    public boolean isRunning() {
        return state != State.CLOSED;
    }

    /**
     * Adds a node to the update list.
     *
     * @param node the node
     */
    final void addToUpdateList(Node node) {
        nodesToUpdateNext.add(node);
    }

    /**
     * Performs a step without noise.
     */
    public void doStep() {
        doStep(false);
    }

    /**
     * Performs a step.
     * This means all nodes which needs a update are updated, and all further nodes to
     * update are also updated until there is no further node to update.
     * So this method propagates a value change through the whole model.
     *
     * @param noise calculation is performed using noise
     */
    public void doStep(boolean noise) {
        stepWithCondition(noise, this::needsUpdate);
    }

    synchronized private void stepWithCondition(boolean noise, StepCondition cond) {
        try {
            if (cond.doNextMicroStep()) {
                int counter = 0;
                while (cond.doNextMicroStep() && state != State.CLOSED) {
                    if (counter++ > MAX_LOOP_COUNTER) {
                        if (oscillatingNodes == null)
                            oscillatingNodes = new HashSet<>();
                        if (counter > COLLECTING_LOOP_COUNTER) {
                            NodeException seemsToOscillate = new NodeException(Lang.get("err_seemsToOscillate")).addNodes(oscillatingNodes);
                            oscillatingNodes = null;
                            throw seemsToOscillate;
                        } else {
                            oscillatingNodes.addAll(nodesToUpdateNext);
                        }
                    }
                    doMicroStep(noise);
                }
            } else
                fireEvent(ModelEvent.STEP);
        } catch (Exception e) {
            errorOccurred(e);
        }
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
     */
    synchronized public void doMicroStep(boolean noise) {
        version++;
        // swap lists
        ArrayList<Node> nl = nodesToUpdateNext;
        nodesToUpdateNext = nodesToUpdateAct;
        nodesToUpdateAct = nl;

        nodesToUpdateNext.clear();

        try {
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
            if (observersMicroStep != null)
                fireEvent(ModelEvent.MICROSTEP);

            if (nodesToUpdateNext.isEmpty())
                fireEvent(ModelEvent.STEP);
        } catch (Exception e) {
            errorOccurred(e);
        }
    }

    /**
     * Runs the model until a positive edge at the break element is detected.
     *
     * @return The number of clock cycles necessary to get the positive edge
     */
    public BreakInfo runToBreak() {
        ArrayList<BreakDetector> brVal = new ArrayList<>();
        for (Break b : breaks)
            brVal.add(new BreakDetector(b));

        ObservableValue clkVal = clocks.get(0).getClockOutput();

        fireEvent(ModelEvent.FASTRUN);
        try {
            while (state != State.CLOSED) {
                clkVal.setBool(!clkVal.getBool());
                doStep();
                for (BreakDetector bd : brVal)
                    if (bd.detected()) {
                        fireEvent(ModelEvent.BREAK);
                        return bd.createInfo();
                    }
            }
        } catch (Exception e) {
            errorOccurred(e);
        }
        return null;
    }

    /**
     * Runs the model until a positive edge at the break element is detected in micro step mode.
     */
    public void runToBreakMicro() {
        ArrayList<BreakDetector> brVal = new ArrayList<>();
        for (Break b : breaks)
            brVal.add(new BreakDetector(b));

        if (brVal.isEmpty()) {
            // simply stabilize the circuit
            doStep();
        } else {
            ObservableValue clkVal = null;
            if (clocks.size() == 1)
                clkVal = clocks.get(0).getClockOutput();

            fireEvent(ModelEvent.FASTRUN);
            final boolean[] wasBreak = {false};
            while (!wasBreak[0] && state != State.CLOSED) {
                if (!needsUpdate()) {
                    if (clkVal != null)
                        clkVal.setBool(!clkVal.getBool());
                    else
                        break;
                }
                stepWithCondition(false, () -> {
                    for (BreakDetector bd : brVal)
                        if (bd.detected()) {
                            fireEvent(ModelEvent.BREAK);
                            wasBreak[0] = true;
                        }
                    return needsUpdate() && !wasBreak[0];
                });
            }
        }
    }

    /**
     * @return true if the models allows fast run steps
     */
    public boolean isRunToBreakAllowed() {
        return clocks.size() == 1 && !breaks.isEmpty();
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
     * The events this observer needs to be called are needed to be given.
     * You have to check for the correct event in the event handler also, because the event handler is
     * maybe called on more events than given.
     *
     * @param observer the observer to add
     * @param event    the mandatory event
     * @param events   more optional events
     */
    public void addObserver(ModelStateObserver observer, ModelEventType event, ModelEventType... events) {
        addObserverForEvent(observer, event);
        for (ModelEventType ev : events)
            addObserverForEvent(observer, ev);
    }

    /**
     * Adds an observer to this model.
     *
     * @param observer the observer to add
     */
    public void addObserver(ModelStateObserverTyped observer) {
        for (ModelEventType ev : observer.getEvents())
            addObserverForEvent(observer, ev);
    }


    private void addObserverForEvent(ModelStateObserver observer, ModelEventType event) {
        ArrayList<ModelStateObserver> obs = observers;
        if (event == ModelEventType.STEP) {
            if (observersStep == null)
                observersStep = new ArrayList<>();
            obs = observersStep;
        } else if (event == ModelEventType.MICROSTEP) {
            if (observersMicroStep == null)
                observersMicroStep = new ArrayList<>();
            obs = observersMicroStep;
        }

        if (!obs.contains(observer))
            obs.add(observer);
    }

    /**
     * Removes an observer to this model.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(ModelStateObserver observer) {
        observers.remove(observer);
        if (observersStep != null)
            observersStep.remove(observer);
        if (observersMicroStep != null)
            observersMicroStep.remove(observer);
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
        if (observersStep != null)
            for (ModelStateObserver mso : observersStep)
                if (mso.getClass() == observerClass)
                    return (T) mso;
        if (observersMicroStep != null)
            for (ModelStateObserver mso : observersMicroStep)
                if (mso.getClass() == observerClass)
                    return (T) mso;
        return null;
    }

    private void fireEvent(ModelEvent event) {
        switch (event.getType()) {
            case MICROSTEP:
                if (observersMicroStep != null)
                    for (ModelStateObserver observer : observersMicroStep)
                        observer.handleEvent(event);
                break;
            case STEP:
                if (observersStep != null)
                    for (ModelStateObserver observer : observersStep)
                        observer.handleEvent(event);
                break;
            default:
                for (ModelStateObserver observer : observers)
                    observer.handleEvent(event);
        }
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
        if (aBreak.isEnabled())
            breaks.add(aBreak);
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
            if (signals.contains(signal))
                invalidSignal = signal;
            signals.add(signal);
            inputs.add(signal);
        } else
            invalidSignal = signal;
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
            if (signals.contains(signal))
                invalidSignal = signal;
            signals.add(signal);
            outputs.add(signal);
        } else
            invalidSignal = signal;
    }

    /**
     * Checks for invalid signals.
     *
     * @throws AnalyseException AnalyseException
     */
    public void checkForInvalidSignals() throws AnalyseException {
        if (invalidSignal != null) {
            String name = invalidSignal.getName();
            if (name == null || name.trim().length() == 0)
                throw new AnalyseException(Lang.get("err_thereIsAUnnamedIO"));
            else
                throw new AnalyseException(Lang.get("err_NameOfIOIsInvalidOrNotUnique_N", name));
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
     * Adds a button which is to map to a keyboard key
     *
     * @param button  the button
     * @param keyCode the key code
     */
    public void addButtonToMap(Button button, int keyCode) {
        buttonsToMap.put(keyCode, button);
    }

    /**
     * Gets the button which matches a givrn key
     *
     * @param keyCode the key
     * @return the button or null if not present
     */
    public Button getButtonToMap(int keyCode) {
        return buttonsToMap.get(keyCode);
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
     * replaces a node by an other node
     *
     * @param oldNode old node
     * @param newNode new node
     * @throws NodeException NodeException
     */
    public void replace(Node oldNode, Node newNode) throws NodeException {
        int i = nodes.indexOf(oldNode);
        if (i < 0)
            throw new NodeException("node not found", oldNode, -1, null);
        nodes.set(i, newNode);
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

    /**
     * Registers a global value.
     *
     * @param name  the name
     * @param value the value
     */
    public void registerGlobalValue(String name, ObservableValue value) {
        if (allowGlobalValues)
            GlobalValues.getInstance().register(name, value, this);
    }

    /**
     * Set or denies the creation of global values.
     *
     * @param allowGlobalValues if true, global values are published
     * @return this for chained calls
     */
    public Model setAllowGlobalValues(boolean allowGlobalValues) {
        this.allowGlobalValues = allowGlobalValues;
        return this;
    }

    /**
     * Sets async execution infos
     *
     * @param asyncInfos essentially the frequency
     */
    public void setAsyncInfos(AsyncSeq asyncInfos) {
        this.asyncInfos = asyncInfos;
    }

    /**
     * @return the infos used for async execution
     */
    public AsyncSeq getAsyncInfos() {
        return asyncInfos;
    }

    @Override
    public <A extends Runnable> A modify(A run) {
        synchronized (this) {
            run.run();
        }
        fireEvent(ModelEvent.EXTERNALCHANGE);
        return run;
    }

    @Override
    public <A extends Runnable> A read(A run) {
        synchronized (this) {
            run.run();
        }
        return run;
    }

    /**
     * Used to return the break info
     */
    public static final class BreakInfo {
        private final int steps;
        private final String label;

        private BreakInfo(int steps, String label) {
            this.steps = steps;
            this.label = label;
        }

        /**
         * @return the number of steps used to reach a break point
         */
        public int getSteps() {
            return steps;
        }

        /**
         * @return the break point reached
         */
        public String getLabel() {
            return label;
        }
    }

    private static final class BreakDetector {
        private final ObservableValue brVal;
        private final int count;
        private final String label;
        private boolean lastIn;
        private int c;

        private BreakDetector(Break breakComp) {
            label = breakComp.getLabel();
            count = breakComp.getCycles() * 2;
            brVal = breakComp.getBreakInput();
            lastIn = brVal.getBool();
            c = 0;
        }

        private boolean detected() throws NodeException {
            c++;
            if (c >= count)
                throw new NodeException(Lang.get("err_breakTimeOut", c, label), brVal);

            boolean aktIn = brVal.getBool();
            if (!lastIn && aktIn) {
                lastIn = aktIn;
                return true;
            } else {
                lastIn = aktIn;
                return false;
            }
        }

        private BreakInfo createInfo() {
            return new BreakInfo(c, label);
        }
    }

    private interface StepCondition {
        boolean doNextMicroStep() throws NodeException;
    }
}
