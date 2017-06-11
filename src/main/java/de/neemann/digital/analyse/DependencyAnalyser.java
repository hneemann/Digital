package de.neemann.digital.analyse;

import de.neemann.digital.core.*;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.util.*;

import static de.neemann.digital.core.Model.MAX_LOOP_COUNTER;

/**
 * Used to analyse on which inputs a given output depends.
 * So you only have to take into account the inputs, a given outputs
 * depends on.
 * Created by hneemann on 11.06.17.
 */
public class DependencyAnalyser {

    private final HashMap<Signal, Set<ObservableValue>> sigMap;

    /**
     * Creates a new instance
     *
     * @param modelAnalyser the model analyser
     * @throws BackTracException BackTracException
     * @throws PinException      PinException
     */
    public DependencyAnalyser(ModelAnalyser modelAnalyser) throws BackTracException, PinException {
        sigMap = new HashMap<>();
        for (Signal s : modelAnalyser.getInputs()) {
            Set<ObservableValue> effected = new HashSet<>();
            backTrac(s.getValue(), effected, MAX_LOOP_COUNTER);
            sigMap.put(s, effected);
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
        for (Map.Entry<Signal, Set<ObservableValue>> e : sigMap.entrySet()) {
            if (e.getValue().contains(output.getValue())) {
                list.add(e.getKey());
            }
        }
        return list;
    }

    private void backTrac(ObservableValue value, Set<ObservableValue> effected, int depth) throws PinException, BackTracException {
        effected.add(value);

        if (depth < 0)
            throw new BackTracException(Lang.get("err_backtracLoopFound"));

        for (de.neemann.digital.core.Observer o : value) {
            if ((o instanceof NodeInterface)) {
                ObservableValues outputs = ((NodeInterface) o).getOutputs();
                for (ObservableValue co : outputs)
                    backTrac(co, effected, depth - 1);
            } else
                throw new BackTracException(Lang.get("err_backtracOf_N_isImpossible", o.getClass().getSimpleName()));
        }
    }

}
