/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.builder.BuilderCollector;
import de.neemann.digital.builder.BuilderException;
import de.neemann.digital.builder.PinMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Used to avoid a double cell allocation if the output of a ff is used as a autput of the state machine.
 * This is the case if a Moore machine is build where the state equals the output.
 * Sometimes this kind of state machines is called a Medwedew machine.
 */
public class BuilderCollectorGAL extends BuilderCollector {
    private final PinMap pinMap;
    private HashSet<String> sequentialVars;
    private boolean doubleCellUsageFixed = false;

    /**
     * Creates a new instance
     *
     * @param pinMap the used pinMap. Is required to handle aliases correctly
     */
    public BuilderCollectorGAL(PinMap pinMap) {
        this.pinMap = pinMap;
        sequentialVars = new HashSet<>();
    }

    @Override
    public BuilderCollector addCombinatorial(String name, Expression expression) throws BuilderException {
        checkOpen();
        return super.addCombinatorial(name, expression);
    }

    @Override
    public BuilderCollector addSequential(String name, Expression expression) throws BuilderException {
        checkOpen();
        sequentialVars.add(name);
        return super.addSequential(name, expression);
    }

    private void checkOpen() {
        if (doubleCellUsageFixed)
            throw new RuntimeException("wrong BuilderCollectorGAL usage!");
    }

    private void fixDoubleCellUsage() {
        if (!doubleCellUsageFixed) {

            super.getCombinatorial().entrySet().removeIf(c -> {
                if (pinMap.isSimpleAlias(c.getKey(), c.getValue(), sequentialVars)) {
                    removeOutput(c.getKey());
                    return true;
                }
                return false;
            });

            doubleCellUsageFixed = true;
        }
    }

    @Override
    public ArrayList<String> getOutputs() {
        fixDoubleCellUsage();
        return super.getOutputs();
    }

    @Override
    public ArrayList<String> getInputs() {
        fixDoubleCellUsage();
        return super.getInputs();
    }

    @Override
    public Map<String, Expression> getCombinatorial() {
        fixDoubleCellUsage();
        return super.getCombinatorial();
    }

    @Override
    public Map<String, Expression> getRegistered() {
        fixDoubleCellUsage();
        return super.getRegistered();
    }
}
