package de.neemann.digital;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;

import static org.junit.Assert.assertEquals;

/**
 * @author hneemann
 */
public class TestExecuter {

    private final Model model;
    private ObservableValue[] inputs;
    private ObservableValue[] outputs;

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
    }

    public TestExecuter setInputs(ObservableValue... values) {
        inputs = values;
        return this;
    }

    public TestExecuter setOutputs(ObservableValue... values) {
        outputs = values;
        return this;
    }

    public TestExecuter setOutputsOf(Element element) {
        outputs = element.getOutputs();
        return this;
    }

    public void check(int... val) throws NodeException {
        for (int i = 0; i < inputs.length; i++) {
            inputs[i].setValue(val[i]);
        }
        if (model != null)
            model.doStep();

        for (int i = 0; i < outputs.length; i++) {
            int should = val[i + inputs.length];
            if (should >= 0)
                assertEquals("output " + i, outputs[i].getValueBits(should), outputs[i].getValue());
        }
    }

}
