package de.neemann.digital;

import static org.junit.Assert.assertEquals;

/**
 * @author hneemann
 */
public class TestExecuter {

    private final Model model;
    private ObservableValue[] inputs;
    private ObservableValue[] outputs;

    public TestExecuter(Model model) throws NodeException {
        this(model, false);
    }

    public TestExecuter(Model model, boolean noise) throws NodeException {
        this.model = model;
        model.init(noise);
    }

    public TestExecuter setInputs(ObservableValue... values) {
        inputs = values;
        return this;
    }

    public TestExecuter setOutputs(ObservableValue... values) {
        outputs = values;
        return this;
    }

    public void check(int... val) throws NodeException {
        for (int i = 0; i < inputs.length; i++) {
            inputs[i].setValue(val[i]);
        }
        model.doStep();

        for (int i = 0; i < outputs.length; i++) {
            assertEquals("Value " + i, outputs[i].getValueBits(val[i + inputs.length]), outputs[i].getValueBits());
        }
    }

}
