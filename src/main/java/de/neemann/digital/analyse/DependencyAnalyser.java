/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.*;
import de.neemann.digital.core.Observer;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * Used to analyse on which inputs a given output depends on.
 * So you only have to take into account the inputs, a given output
 * really depends on.
 */
public class DependencyAnalyser {
    private final HashMap<Signal, Set<ObservableValue>> dependencyMap;

    /**
     * Creates a new instance
     *
     * @param modelAnalyser the model analyser
     * @throws BacktrackException BacktrackException
     * @throws PinException       PinException
     */
    public DependencyAnalyser(ModelAnalyser modelAnalyser) throws BacktrackException, PinException {
        dependencyMap = new HashMap<>();
        for (Signal s : modelAnalyser.getInputs()) {
            Set<ObservableValue> effected = new HashSet<>();
            backtracking(s.getValue(), effected);
            dependencyMap.put(s, effected);
        }
    }

    /**
     * Returns all inputs the given output depends on
     *
     * @param output the output to analyse
     * @return the list of inputs which effect the given output
     */
    public ArrayList<Signal> getInputs(Signal output) {
        ArrayList<Signal> list = new ArrayList<>();
        for (Map.Entry<Signal, Set<ObservableValue>> e : dependencyMap.entrySet()) {
            if (e.getValue().contains(output.getValue()))
                list.add(e.getKey());
        }
        return list;
    }

    /**
     * Returns the number of model steps needed to analyse the model.
     *
     * @param modelAnalyser the model analyser
     * @return the number of required steps
     */
    public long getRequiredSteps(ModelAnalyser modelAnalyser) {
        long num = 0;
        for (Signal o : modelAnalyser.getOutputs()) {
            int n = getInputs(o).size();
            num += (1L << n);
        }
        return num;
    }

    private void backtracking(ObservableValue value, Set<ObservableValue> effected) throws PinException, BacktrackException {
        if (!effected.contains(value)) {
            effected.add(value);

            for (Observer o : value.getObservers()) {
                if ((o instanceof NodeInterface)) {
                    ObservableValues outputs = ((NodeInterface) o).getOutputs();
                    for (ObservableValue co : outputs)
                        backtracking(co, effected);
                } else
                    throw new BacktrackException(Lang.get("err_backtrackOf_N_isImpossible", o.getClass().getSimpleName()));
            }
        }
    }
}
