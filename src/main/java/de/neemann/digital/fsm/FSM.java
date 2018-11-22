/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

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

    /**
     * Creates the truth table which is defined by this finite state machine
     *
     * @return the truth table
     * @throws ExpressionException        ExpressionException
     * @throws FinitStateMachineException FinitStateMachineException
     */
    public TruthTable createTruthTable() throws ExpressionException, FinitStateMachineException {
        int stateBits = getStateVarBits();

        // create state variables
        ArrayList<Variable> vars = new ArrayList<>();
        for (int i = stateBits - 1; i >= 0; i--)
            vars.add(new Variable("Q^" + i + "_n"));

        TruthTable truthTable = new TruthTable(vars);

        // create the next state result variables
        for (int i = stateBits - 1; i >= 0; i--)
            truthTable.addResult("Q^" + i + "_n+1");

        // add the output variables
        TreeSet<String> results = new TreeSet<>();
        for (State s : states)
            results.addAll(s.getValues().keySet());

        for (String name : results)
            truthTable.addResult(name);

        // set all to dc
        truthTable.setAllTo(2);

        // set output variables
        for (State s : states) {
            int row = s.getNumber();
            int col = stateBits * 2;
            for (String name : results) {
                Long val = s.getValues().get(name);
                long v = val == null ? 0 : val;
                truthTable.setValue(row, col, (int) v);
                col++;
            }
        }


        // set all next state variables to "stay is state"
        for (State s : states) {
            int c = stateBits * 2;
            int row = s.getNumber();
            int m = row;
            for (int j = 0; j < stateBits; j++) {
                c--;
                truthTable.setValue(row, c, m & 1);
                m >>= 1;
            }
        }

        // add the additional input variables
        VariableVisitor vv = new VariableVisitor();
        for (Transition t : transitions)
            if (t.getCondition() != null)
                t.getCondition().traverse(vv);
        ArrayList<Variable> inVars = new ArrayList<>(vv.getVariables());

        for (Variable v : inVars)
            truthTable.addVariable(v);

        int rowsPerState = 1 << inVars.size();

        // fill in the transitions
        for (Transition t : transitions) {
            int startState = t.getStartState().getNumber();
            int startRow = startState * rowsPerState;
            ContextMap c = new ContextMap();
            for (int r = 0; r < rowsPerState; r++) {
                int m = 1 << (inVars.size() - 1);
                for (Variable v : inVars) {
                    c.set(v, (r & m) != 0);
                    m >>= 1;
                }
                if (t.getCondition() == null || t.getCondition().calculate(c)) {
                    int col = stateBits * 2 + inVars.size();
                    int row = startRow + r;
                    int mask = t.getTargetState().getNumber();
                    for (int j = 0; j < stateBits; j++) {
                        col--;
                        truthTable.setValue(row, col, mask & 1);
                        mask >>= 1;
                    }
                }
            }
        }

        return truthTable;

    }

    private int getStateVarBits() throws FinitStateMachineException {
        HashSet<Integer> numbers = new HashSet<>();
        int maxNumber = 0;
        for (State s : states) {
            final int n = s.getNumber();
            if (n > maxNumber)
                maxNumber = n;

            if (numbers.contains(n))
                throw new FinitStateMachineException(Lang.get("err_fsmNumberUsedTwice_N", n));
            numbers.add(n);
        }

        if (!numbers.contains(0))
            throw new FinitStateMachineException(Lang.get("err_fsmNoInitialState"));

        int n = 1;
        while ((1 << n) <= maxNumber) n++;
        return n;
    }
}
