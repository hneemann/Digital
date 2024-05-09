/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.*;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * Used to determine the max path len in the circuit.
 * This means the max number of gates between on of the inputs and one of the outputs.
 */
public class PathLenAnalyser {
    private int maxDepth;

    /**
     * Creates a new instance
     *
     * @param modelAnalyser the model analyser
     * @throws BacktrackException BacktrackException
     * @throws PinException       PinException
     */
    public PathLenAnalyser(ModelAnalyser modelAnalyser) throws BacktrackException, PinException {
        for (Signal s : modelAnalyser.getInputs()) {
            HashMap<ObservableValue, Integer> found = new HashMap<>();
            backtracking(s.getValue(), found, 0);
        }
    }

    private void backtracking(ObservableValue value, HashMap<ObservableValue, Integer> found, int depth) throws PinException, BacktrackException {
        Integer d = found.get(value);
        if (d == null || d < depth) {
            found.put(value, depth);

            if (depth > maxDepth)
                maxDepth = depth;
            for (Observer o : value.getObservers()) {
                if ((o instanceof NodeInterface)) {
                    ObservableValues outputs = ((NodeInterface) o).getOutputs();
                    int de = depth;
                    if (!(o instanceof NodeWithoutDelay)) de++;
                    for (ObservableValue co : outputs)
                        backtracking(co, found, de);
                } else
                    throw new BacktrackException(Lang.get("err_backtrackOf_N_isImpossible", o.getClass().getSimpleName()));
            }
        }
    }

    /**
     * @return the max path len in the circuit
     */
    public int getMaxPathLen() {
        return maxDepth;
    }
}
