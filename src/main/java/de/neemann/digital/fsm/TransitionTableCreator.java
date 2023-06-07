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
import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * Creates a transition table from given states and transitions
 */
public class TransitionTableCreator {
    private static final String STATE_VAR = "Z";

    private final List<State> states;
    private final List<Transition> transitions;
    private final HashMap<Movable, TreeMap<String, Integer>> outputValues;
    private final ArrayList<Signal> inputSignals;
    private final ArrayList<Signal> outputSignals;
    private final int initState;
    private TruthTable truthTable;
    private int rowsPerState;
    private ArrayList<Variable> inVars;
    private int stateBits;
    private boolean[] transitionSet;
    private ModelAnalyserInfo modelAnalyserInfo;
    private CircuitRepresentation cr;

    /**
     * Creates a new instance
     *
     * @param fsm the fsm
     */
    TransitionTableCreator(FSM fsm) throws FiniteStateMachineException {
        this(fsm, null);
    }

    /**
     * Creates a new instance
     *
     * @param fsm             the fsm
     * @param stateSignalName the name if the signal indicating the actual state
     */
    TransitionTableCreator(FSM fsm, String stateSignalName) throws FiniteStateMachineException {
        this.states = fsm.getStates();
        this.transitions = fsm.getTransitions();
        this.initState = fsm.getInitState();
        cr = new CircuitRepresentation();
        fsm.setCircuitRepresentation(cr);
        outputValues = new HashMap<>();
        modelAnalyserInfo = new ModelAnalyserInfo(null);
        modelAnalyserInfo.setStateSignalName(stateSignalName);
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
        cr.setStateVarBits(stateBits);

        // create state variables
        ArrayList<Variable> vars = new ArrayList<>();
        ArrayList<String> maiNames = new ArrayList<>();
        for (int i = stateBits - 1; i >= 0; i--) {
            final Variable var = new Variable(STATE_VAR + "_" + i + "^n");
            maiNames.add(0, var.getIdentifier());
            vars.add(var);
            boolean initVal = (initState & (1 << i)) != 0;
            modelAnalyserInfo.setSequentialInitValue(var.getIdentifier(), initVal ? 1 : 0);
        }

        truthTable = new TruthTable(vars);

        // create the next state result variables
        for (int i = stateBits - 1; i >= 0; i--)
            truthTable.addResult(STATE_VAR + "_" + i + "^{n+1}");

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
        truthTable.modifyValues(v -> (byte) 2);

        // set state output variables
        for (State s : states) {
            int row = s.getNumber();
            cr.addState(row, s);
            int col = stateBits * 2;
            for (String name : results) {
                int def = s.isDefaultDC() ? 2 : 0;
                Integer val = getValues(s).get(name);
                int v = val == null ? def : val;
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
                int aValue = 2;
                if (!s.isDefaultDC())
                    aValue = m & 1;
                truthTable.setValue(row, c, aValue);
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
            maiNames.add(v.getIdentifier());
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
        modelAnalyserInfo.setStateSignalBitNames(maiNames);
        truthTable.setModelAnalyzerInfo(modelAnalyserInfo);
        return truthTable;
    }

    private void fillInTransition(Transition t, TreeSet<String> results) throws ExpressionException, FiniteStateMachineException {
        int startState = t.getStartState().getNumber();
        int startRow = startState * rowsPerState;
        ContextMap c = new ContextMap();
        for (int r = 0; r < rowsPerState; r++) {
            int transVal = 0;
            int transValMask = 1;
            int m = 1 << (inVars.size() - 1);
            for (Variable v : inVars) {
                final boolean b = (r & m) != 0;
                c.set(v, b);
                if (b) transVal |= transValMask;
                m >>= 1;
                transValMask <<= 1;
            }
            if (!t.hasCondition() || t.getConditionExpression().calculate(c)) {
                int n = t.getStartState().getNumber();
                n |= (transVal << stateBits);
                cr.addTransition(n, t);

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

        int n = 1;
        while ((1 << n) <= maxNumber) n++;
        return n;
    }

}
