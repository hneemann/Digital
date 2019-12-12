/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.VariableVisitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Builder implementation which only collects the expressions to build.
 */
public class BuilderCollector implements BuilderInterface<BuilderCollector> {
    private final VariableVisitor vars;
    private ArrayList<String> outputs;
    private TreeMap<String, Expression> combinatorial;
    private TreeMap<String, Expression> registered;

    /**
     * Creates a new instance
     */
    public BuilderCollector() {
        vars = new VariableVisitor();
        outputs = new ArrayList<>();
        combinatorial = new TreeMap<>();
        registered = new TreeMap<>();
    }

    @Override
    public BuilderCollector addCombinatorial(String name, Expression expression) throws BuilderException {
        expression.traverse(vars);
        outputs.add(name);
        combinatorial.put(name, expression);
        return this;
    }

    @Override
    public BuilderCollector addSequential(String name, Expression expression) throws BuilderException {
        expression.traverse(vars);
        outputs.add(name);
        registered.put(name, expression);
        return this;
    }

    /**
     * @return the output names
     */
    public ArrayList<String> getOutputs() {
        return outputs;
    }

    /**
     * @return the input names
     */
    public ArrayList<String> getInputs() {
        ArrayList<String> inputs = new ArrayList<>();
        for (Variable v : vars.getVariables())
            if (!outputs.contains(v.getIdentifier()))
                inputs.add(v.getIdentifier());
        return inputs;
    }

    /**
     * @return the combinatorial expressions
     */
    public Map<String, Expression> getCombinatorial() {
        return combinatorial;
    }

    /**
     * @return the registered expressions
     */
    public Map<String, Expression> getRegistered() {
        return registered;
    }

    /**
     * Removes an output.
     *
     * @param name the output to remove
     */
    public void removeOutput(String name) {
        outputs.remove(name);
    }
}
