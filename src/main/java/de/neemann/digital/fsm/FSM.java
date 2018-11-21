/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.VectorFloat;

import java.util.ArrayList;

/**
 * A simple finite state machine
 */
public class FSM {

    private ArrayList<State> states;
    private ArrayList<Transition> transitions;

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
        state.setNumber(states.size());
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
        return this;
    }

    /**
     * Adds a transition to the FSM
     *
     * @param from      the from state
     * @param to        the to state
     * @param condition the condition
     * @return this for chained calls
     * @throws FinitStateMachineException FinitStateMachineException
     */
    public FSM transition(String from, String to, Expression condition) throws FinitStateMachineException {
        return transition(findState(from), findState(to), condition);
    }

    /**
     * Adds a transition to the FSM
     *
     * @param from      the from state
     * @param to        the to state
     * @param condition the condition
     * @return this for chained calls
     * @throws FinitStateMachineException FinitStateMachineException
     */
    public FSM transition(int from, int to, Expression condition) throws FinitStateMachineException {
        return transition(findState(from), findState(to), condition);
    }

    /**
     * Adds a transition to the FSM
     *
     * @param from      the from state
     * @param to        the to state
     * @param condition the condition
     * @return this for chained calls
     * @throws FinitStateMachineException FinitStateMachineException
     */
    public FSM transition(State from, State to, Expression condition) {
        return add(new Transition(from, to, condition));
    }

    private State findState(String name) throws FinitStateMachineException {
        for (State s : states)
            if (s.getName().equals(name))
                return s;
        throw new FinitStateMachineException("State " + name + " not found!");
    }

    private State findState(int number) throws FinitStateMachineException {
        for (State s : states)
            if (s.getNumber() == number)
                return s;
        throw new FinitStateMachineException("State " + number + " not found!");
    }

    /**
     * Calculates all forces to move the elements
     *
     * @return this for chained calls
     */
    public FSM calculateForces() {
        for (State s : states)
            s.calcExpansionForce(states);
        for (Transition t : transitions)
            t.calcForce(states, transitions);
        return this;
    }

    /**
     * Draws the FSM
     *
     * @param gr the Graphic instance to draw to
     */
    public void drawTo(Graphic gr) {
        for (State s : states)
            s.drawTo(gr);
        for (Transition t : transitions)
            t.drawTo(gr);
    }

    /**
     * Moved the elements
     *
     * @param dt the time step
     */
    public void move(int dt) {
        for (State s : states)
            s.move(dt);
        for (Transition t : transitions)
            t.move(dt);

    }

    /**
     * Orders all states in a big circle
     */
    public void circle() {
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
    }
}
