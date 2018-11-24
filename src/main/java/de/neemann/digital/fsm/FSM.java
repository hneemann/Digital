/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple finite state machine
 */
public class FSM {

    private ArrayList<State> states;
    private ArrayList<Transition> transitions;
    private transient boolean initChecked;
    private transient boolean modified;
    private transient ModifiedListener modifiedListener;

    /**
     * Creates a proper configured XStream instance
     *
     * @return the XStream instance
     */
    public static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("fsm", FSM.class);
        xStream.alias("state", State.class);
        xStream.alias("transition", Transition.class);
        xStream.alias("vector", Vector.class);
        xStream.aliasAttribute(Vector.class, "x", "x");
        xStream.aliasAttribute(Vector.class, "y", "y");
        xStream.alias("vectorf", VectorFloat.class);
        xStream.aliasAttribute(VectorFloat.class, "x", "x");
        xStream.aliasAttribute(VectorFloat.class, "y", "y");
        return xStream;
    }

    /**
     * Creates a new circuit instance from a stored file
     *
     * @param filename filename
     * @return the fsm
     * @throws IOException IOException
     */
    public static FSM loadFSM(File filename) throws IOException {
        return loadFSM(new FileInputStream(filename));
    }

    /**
     * Creates a new fsm instance from a stored file
     *
     * @param in the input stream
     * @return the fsm
     * @throws IOException IOException
     */
    public static FSM loadFSM(InputStream in) throws IOException {
        try {
            XStream xStream = getxStream();
            final FSM fsm = (FSM) xStream.fromXML(in);
            for (Transition t : fsm.transitions)
                t.setFSM(fsm);
            for (State s : fsm.states)
                s.setFSM(fsm);
            fsm.modified = false;
            return fsm;
        } catch (RuntimeException e) {
            throw new IOException(Lang.get("err_invalidFileFormat"), e);
        } finally {
            in.close();
        }
    }

    /**
     * Stores the fsm in the given file
     *
     * @param filename filename
     * @throws IOException IOException
     */
    public void save(File filename) throws IOException {
        save(new FileOutputStream(filename));
    }

    /**
     * Stores the circuit in the given file
     *
     * @param out the writer
     * @throws IOException IOException
     */
    public void save(OutputStream out) throws IOException {
        try (Writer w = new OutputStreamWriter(out, "utf-8")) {
            XStream xStream = getxStream();
            w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(this, new PrettyPrintWriter(w));
            modified = false;
            if (modifiedListener != null)
                modifiedListener.modifiedChanged(modified);
        }
    }

    /**
     * Creates a new FSM containing the given states
     *
     * @param state the states
     */
    public FSM(State... state) {
        states = new ArrayList<>();
        transitions = new ArrayList<>();
        for (State s : state)
            add(s);
    }

    /**
     * Adds a state to the FSM
     *
     * @param state the state to add
     * @return this for chained calls
     */
    public FSM add(State state) {
        if (state.getNumber() < 0)
            state.setNumber(states.size());
        state.setFSM(this);
        states.add(state);
        return this;
    }

    /**
     * Adds a transition to the FSM
     *
     * @param transition the transition to add
     * @return this for chained calls
     */
    public FSM add(Transition transition) {
        transitions.add(transition);
        transition.setFSM(this);
        return this;
    }

    /**
     * Adds a transition to the FSM
     *
     * @param from      the from state
     * @param to        the to state
     * @param condition the condition
     * @return this for chained calls
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public FSM transition(String from, String to, String condition) throws FiniteStateMachineException {
        return transition(findState(from), findState(to), condition);
    }

    /**
     * Adds a transition to the FSM
     *
     * @param from      the from state
     * @param to        the to state
     * @param condition the condition
     * @return this for chained calls
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public FSM transition(int from, int to, String condition) throws FiniteStateMachineException {
        return transition(findState(from), findState(to), condition);
    }

    /**
     * Adds a transition to the FSM
     *
     * @param from      the from state
     * @param to        the to state
     * @param condition the condition
     * @return this for chained calls
     */
    public FSM transition(State from, State to, String condition) {
        if (!states.contains(from))
            add(from);
        if (!states.contains(to))
            add(to);
        return add(new Transition(from, to, condition));
    }

    private State findState(String name) throws FiniteStateMachineException {
        for (State s : states)
            if (s.getName().equals(name))
                return s;
        throw new FiniteStateMachineException(Lang.get("err_fsmState_N_notFound", name));
    }

    private State findState(int number) throws FiniteStateMachineException {
        for (State s : states)
            if (s.getNumber() == number)
                return s;
        throw new FiniteStateMachineException(Lang.get("err_fsmState_N_notFound", number));
    }

    /**
     * Calculates all forces to move the elements
     *
     * @return this for chained calls
     */
    private void calculateForces() {
        for (State s : states)
            s.calcExpansionForce(states);
        for (Transition t : transitions)
            t.calcForce(states, transitions);
    }

    /**
     * @return the states
     */
    public List<State> getStates() {
        return states;
    }

    /**
     * Draws the FSM
     *
     * @param gr the Graphic instance to draw to
     */
    public void drawTo(Graphic gr) {
        if (!initChecked) {
            checkInitState();
            initChecked = true;
        }
        for (State s : states)
            s.drawTo(gr);
        for (Transition t : transitions)
            t.drawTo(gr);
    }

    private void checkInitState() {
        int count = 0;
        Transition found = null;
        for (Transition t : transitions) {
            if (t.getStartState().getNumber() == 0) {
                count++;
                found = t;
            }
        }
        try {
            if (count == 1 && !found.hasCondition()) {
                found.getStartState().setInitial();
                found.setInitial();
            }
        } catch (FiniteStateMachineException e) {
            // ignore
        }
    }

    /**
     * Moved the elements
     *
     * @param dt         the time step
     * @param moveStates if true also states are moved
     * @param except     element which is fixed
     */
    public void move(int dt, boolean moveStates, Movable except) {
        calculateForces();
        if (moveStates)
            for (State s : states)
                if (s != except)
                    s.move(dt);
        for (Transition t : transitions)
            if (t != except)
                t.move(dt);
    }

    /**
     * Orders all states in a big circle
     *
     * @return this for chained calls
     */
    public FSM circle() {
        double delta = 2 * Math.PI / states.size();
        double rad = 0;
        for (State s : states)
            if (s.getRadius() > rad)
                rad = s.getRadius();

        rad *= 4;
        double phi = 0;
        for (State s : states) {
            s.setPosition(new VectorFloat((float) (Math.sin(phi) * rad), (float) (-Math.cos(phi) * rad)));
            phi += delta;
        }

        for (Transition t : transitions)
            t.initPos();

        return this;
    }

    /**
     * Creates the truth table which is defined by this finite state machine
     *
     * @return the truth table
     * @throws ExpressionException         ExpressionException
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public TruthTable createTruthTable() throws ExpressionException, FiniteStateMachineException {
        return new TransitionTableCreator(this).create();
    }

    /**
     * Returns the element at the given position
     *
     * @param pos the position
     * @return the element or null
     */
    public Movable getMovable(Vector pos) {
        for (State s : states)
            if (s.matches(pos))
                return s;

        for (Transition t : transitions)
            if (t.matches(pos))
                return t;

        return null;
    }

    /**
     * Move states to raster
     */
    public void toRaster() {
        for (State s : states)
            s.toRaster();
    }

    /**
     * @return the transitions
     */
    public List<Transition> getTransitions() {
        return transitions;
    }

    /**
     * Removes the given transition
     *
     * @param transition the transition to remove
     */
    public void remove(Transition transition) {
        transitions.remove(transition);
        wasModified();
    }

    /**
     * Removes the given state
     *
     * @param state the state to remove
     */
    public void remove(State state) {
        states.remove(state);
        transitions.removeIf(t -> t.getStartState() == state || t.getTargetState() == state);
        wasModified();
    }

    /**
     * Marks the fsm as modified
     */
    void wasModified() {
        modified = true;
        if (modifiedListener != null)
            modifiedListener.modifiedChanged(modified);
    }

    /**
     * Sets a modified listener
     *
     * @param modifiedListener the listener called if fsm was modified
     */
    public void setModifiedListener(ModifiedListener modifiedListener) {
        this.modifiedListener = modifiedListener;
    }

    /**
     * @return true if fsm has changed
     */
    public boolean hasChanged() {
        return modified;
    }

    /**
     * a modified listener
     */
    public interface ModifiedListener {
        /**
         * called if fsm was modified
         *
         * @param wasModified true is fsm is modified
         */
        void modifiedChanged(boolean wasModified);
    }
}
