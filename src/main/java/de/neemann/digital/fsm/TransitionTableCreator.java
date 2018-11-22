/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextMap;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.VariableVisitor;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Creates a transition table from given states and transitions
 */
public class TransitionTableCreator {
    private final List<State> states;
    private final List<Transition> transitions;
    private TruthTable truthTable;
    private int rowsPerState;
    private ArrayList<Variable> inVars;
    private int stateBits;
    private boolean[] transitionSet;

    /**
     * Creates a new instance
     *
     * @param fsm the fsm
     */
    public TransitionTableCreator(FSM fsm) {
        this.states = fsm.getStates();
        this.transitions = fsm.getTransitions();
    }

    /**
     * Creates the transition table
     *
     * @return the transition table
     * @throws FinitStateMachineException FinitStateMachineException
     * @throws ExpressionException        ExpressionException
     */
    public TruthTable create() throws FinitStateMachineException, ExpressionException {
        stateBits = getStateVarBits();

        // create state variables
        ArrayList<Variable> vars = new ArrayList<>();
        for (int i = stateBits - 1; i >= 0; i--)
            vars.add(new Variable("Q" + i + "_n"));

        truthTable = new TruthTable(vars);

        // create the next state result variables
        for (int i = stateBits - 1; i >= 0; i--)
            truthTable.addResult("Q" + i + "_n+1");

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
            if (t.hasCondition())
                t.getConditionExpression().traverse(vv);
        inVars = new ArrayList<>(vv.getVariables());

        for (Variable v : inVars)
            truthTable.addVariable(v);

        rowsPerState = 1 << inVars.size();

        // fill in the unconditional transitions
        for (Transition t : transitions)
            if (!t.hasCondition())
                fillInTransition(t);

        transitionSet = new boolean[truthTable.getRows()];

        // fill in the conditional transitions
        for (Transition t : transitions)
            if (t.hasCondition())
                fillInTransition(t);

        return truthTable;
    }

    private void fillInTransition(Transition t) throws ExpressionException, FinitStateMachineException {
        int startState = t.getStartState().getNumber();
        int startRow = startState * rowsPerState;
        ContextMap c = new ContextMap();
        for (int r = 0; r < rowsPerState; r++) {
            int m = 1 << (inVars.size() - 1);
            for (Variable v : inVars) {
                c.set(v, (r & m) != 0);
                m >>= 1;
            }
            if (!t.hasCondition() || t.getConditionExpression().calculate(c)) {
                int col = stateBits * 2 + inVars.size();
                int row = startRow + r;

                checkRow(row, t);

                int mask = t.getTargetState().getNumber();
                for (int j = 0; j < stateBits; j++) {
                    col--;
                    truthTable.setValue(row, col, mask & 1);
                    mask >>= 1;
                }
            }
        }
    }

    private void checkRow(int row, Transition t) throws FinitStateMachineException {
        if (transitionSet != null) {
            if (transitionSet[row])
                throw new FinitStateMachineException(Lang.get("err_notDeterministic_N_N", t.getStartState(), t.getTargetState()));
            transitionSet[row] = true;
        }
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
