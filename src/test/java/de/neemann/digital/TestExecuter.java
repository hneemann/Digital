/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.ModelEntry;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.integration.Resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class TestExecuter {
    public static final Object IGNORE = new Object();
    public static final Object HIGHZ = new Object();

    private final Model model;
    private ArrayList<ObservableValue> inputs;
    private ArrayList<ObservableValue> outputs;

    public static TestExecuter createFromFile(String name, ElementLibrary library) throws IOException, NodeException, PinException, ElementNotFoundException {
        File filename = new File(Resources.getRoot(), name);
        Circuit circuit = Circuit.loadCircuit(filename, new ShapeFactory(library));

        ModelCreator mb = new ModelCreator(circuit, library);

        return new TestExecuter(mb.createModel(false), true).setUp(mb);
    }


    public TestExecuter() throws NodeException {
        this(null);
    }

    public TestExecuter(Model model) throws NodeException {
        this(model, false);
    }

    public TestExecuter(Model model, boolean noise) throws NodeException {
        this.model = model;
        if (model != null)
            model.init(noise);
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public TestExecuter setInputs(ObservableValues values) {
        inputs.addAll(values);
        return this;
    }

    public TestExecuter setInputs(ObservableValue... values) {
        inputs.addAll(Arrays.asList(values));
        return this;
    }

    public TestExecuter setInputs(List<ModelEntry> entries) {
        for (ModelEntry me : entries)
            setInputs(me);
        return this;
    }

    private void setInputs(ModelEntry me) {
        setInputs(me.getIoState().getOutputs());
    }


    public TestExecuter setOutputs(ObservableValues values) {
        outputs.addAll(values);
        return this;
    }

    public TestExecuter setOutputs(ObservableValue... values) {
        outputs.addAll(Arrays.asList(values));
        return this;
    }

    public TestExecuter setOutputs(List<ModelEntry> entries) {
        for (ModelEntry me : entries)
            setOutputs(me);
        return this;
    }

    private void setOutputs(ModelEntry me) {
        setOutputs(me.getIoState().getInputs());
    }

    public TestExecuter setUp(ModelCreator modelCreator) {
        List<ModelEntry> inputs = modelCreator.getEntries("In");
        List<ModelEntry> outputs = modelCreator.getEntries("Out");

        for (ModelEntry input : inputs) {
            assertEquals(0, input.getIoState().inputCount());
            assertEquals(1, input.getIoState().outputCount());
        }
        for (ModelEntry output : outputs) {
            assertEquals(1, output.getIoState().inputCount());
            assertEquals(0, output.getIoState().outputCount());
        }

        setInputs(inputs);
        setOutputs(outputs);
        return this;
    }

    public TestExecuter setOutputsOf(Element element) throws PinException {
        setOutputs(element.getOutputs());
        return this;
    }

    public void checkC(long... val) throws NodeException {
        ObservableValue clock = model.getClocks().get(0).getClockOutput();
        clock.setBool(true);
        model.doStep();
        clock.setBool(false);
        check(val);
    }

    public void checkZ(Object... val) throws NodeException {
        for (int i = 0; i < inputs.size(); i++) {
            if (val[i] == HIGHZ)
                inputs.get(i).setToHighZ();
            else
                inputs.get(i).setValue(((Number) val[i]).longValue());
        }
        if (model != null)
            model.doStep();

        for (int i = 0; i < outputs.size(); i++) {
            final Object v = val[i + inputs.size()];
            if (v != IGNORE) {
                if (v == HIGHZ) {
                    assertTrue("highz output " + i, outputs.get(i).isHighZ());
                } else {
                    long should = ((Number) v).longValue();
                    assertEquals("output " + i, outputs.get(i).getValueBits(should), outputs.get(i).getValue());
                }
            }
        }
    }

    public void check(long... val) throws NodeException {
        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).setValue(val[i]);
        if (model != null)
            model.doStep();

        for (int i = 0; i < outputs.size(); i++) {
            long should = val[i + inputs.size()];
            assertEquals("output " + i, outputs.get(i).getValueBits(should), outputs.get(i).getValue());
        }
    }


    public void clockUntil(int... val) throws NodeException {
        for (int clocks = 0; clocks < 1000; clocks++) {
            boolean isReached = true;
            for (int i = 0; i < outputs.size(); i++) {
                if (outputs.get(i).getValue() != val[i])
                    isReached = false;
            }
            if (isReached)
                return;

            inputs.get(0).setValue(1 - inputs.get(0).getValue());
            model.doStep();
        }
        throw new RuntimeException("desired state is not reached!");
    }

    public Model getModel() {
        return model;
    }
}
