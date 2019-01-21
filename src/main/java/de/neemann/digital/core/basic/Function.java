/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

import java.util.ArrayList;

/**
 */
abstract class Function extends FanIn {

    private long value;

    Function(int bits) {
        super(bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = calculate(getInputs());
    }

    @Override
    public void writeOutputs() throws NodeException {
        getOutput().setValue(value);
    }

    protected abstract long calculate(ArrayList<ObservableValue> inputs) throws NodeException;

}
