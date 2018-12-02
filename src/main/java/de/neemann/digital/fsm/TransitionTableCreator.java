/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextMap;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.VariableVisitor;
import de.neemann.digital.core.Signal;
import de.neemann.digital.fsm.gui.FSMFrame;
import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * Creates a transition table from given states and transitions
 */
public class TransitionTableCreator {
    private final List<State> states;
    private final List<Transition> transitions;
    private final HashMap<Movable, TreeMap<String, Integer>> outputValues;
    private final ArrayList<Signal> inputSignals;
    private final ArrayList<Signal> outputSignals;
    private TruthTable truthTable;
    private int rowsPerState;
    private ArrayList<Variable> inVars;
    private int stateBits;
    private boolean[] transitionSet;
    private ModelAnalyserInfo modelAnalyserInfo;

    /**
     * Creates a new instance
     *
     * @param fsm the fsm
     */
    TransitionTableCreator(FSM fsm) {
        this(fsm, null);
    }

    /**
     * Creates a new instance
     *
     * @param fsm     the fsm
     * @param creator the creating frame
     */
    TransitionTableCreator(FSM fsm, FSMFrame creator) {
        this.states = fsm.getStates();
        this.transitions = fsm.getTransitions();
        outputValues = new HashMap<>();
        modelAnalyserInfo = new ModelAnalyserInfo(null);
        modelAnalyserInfo.setMainCreatedNotification(new FSMStateInfo(fsm.getFile(), creator));
        inputSignals = new ArrayList<>();
        outputSignals = new ArrayList<>();
    }

    private TreeMap<String, Integer> getValues(Movable m) throws FiniteStateMachineException {
        TreeMap<String, Integer> values = outputValues.get(m);
        if (values == null) {
            values = new ValueParser(m.getValues()).setModelAnalyzerInfo(modelAnalyserInfo).parse();
            outputValues.put(m, values);
        }
        return values;
    }

    /**
     * Creates the transition table
     *
     * @return the transition table
     * @throws FiniteStateMachineException FiniteStateMachineException
     * @throws ExpressionException         ExpressionException
     */
    public TruthTable create() throws FiniteStateMachineException, ExpressionException {
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
            results.addAll(getValues(s).keySet());
        for (Transition t : transitions)
            results.addAll(getValues(t).keySet());

        for (String name : results) {
            truthTable.addResult(name);
            outputSignals.add(new Signal(name, null));
        }

        // set all to dc
        truthTable.setAllTo(2);

        // set state output variables
        for (State s : states) {
            int row = s.getNumber();
            int col = stateBits * 2;
            for (String name : results) {
                Integer val = getValues(s).get(name);
                int v = val == null ? 0 : val;
                truthTable.setValue(row, col, v);
                col++;
            }
        }

        // set all next state results to "stay is state"
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

        for (Variable v : inVars) {
            truthTable.addVariable(v);
            inputSignals.add(new Signal(v.getIdentifier(), null));
        }

        rowsPerState = 1 << inVars.size();

        transitionSet = new boolean[truthTable.getRows()];

        // fill in the unconditional transitions
        for (Transition t : transitions)
            if (!t.hasCondition())
                fillInTransition(t, results);

        transitionSet = new boolean[truthTable.getRows()];

        // fill in the conditional transitions
        for (Transition t : transitions)
            if (t.hasCondition())
                fillInTransition(t, results);

        modelAnalyserInfo.setInOut(inputSignals, outputSignals);
        truthTable.setModelAnalyzerInfo(modelAnalyserInfo);
        return truthTable;
    }

    private void fillInTransition(Transition t, TreeSet<String> results) throws ExpressionException, FiniteStateMachineException {
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

                checkRow(row, t);  // allow only deterministic transitions

                // fill in transition
                int mask = t.getTargetState().getNumber();
                for (int j = 0; j < stateBits; j++) {
                    col--;
                    truthTable.setValue(row, col, mask & 1);
                    mask >>= 1;
                }

                // fill in output state, if any
                final TreeMap<String, Integer> valueMap = getValues(t);
                if (!valueMap.isEmpty()) {
                    col = stateBits * 2 + inVars.size();
                    for (String name : results) {
                        Integer val = valueMap.get(name);
                        if (val != null)
                            truthTable.setValue(row, col, val);
                        col++;
                    }
                }
            }
        }
    }

    private void checkRow(int row, Transition t) throws FiniteStateMachineException {
        if (transitionSet[row])
            throw new FiniteStateMachineException(Lang.get("err_notDeterministic_N", t.toString()));
        transitionSet[row] = true;
    }

    private int getStateVarBits() throws FiniteStateMachineException {
        HashSet<Integer> numbers = new HashSet<>();
        int maxNumber = 0;
        for (State s : states) {
            final int n = s.getNumber();
            if (n > maxNumber)
                maxNumber = n;

            if (numbers.contains(n))
                throw new FiniteStateMachineException(Lang.get("err_fsmNumberUsedTwice_N", n));
            numbers.add(n);
        }

        if (!numbers.contains(0))
            throw new FiniteStateMachineException(Lang.get("err_fsmNoInitialState"));

        int n = 1;
        while ((1 << n) <= maxNumber) n++;
        return n;
    }

}
