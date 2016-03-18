package de.neemann.digital.core.basic;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.*;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public abstract class FanIn extends Node implements Part {

    protected final ArrayList<ObservableValue> inputs;
    protected final ObservableValue output;

    public FanIn(int bits) {
        inputs = new ArrayList<>();
        output = new ObservableValue("out", bits);
    }

    public ObservableValue getOutput() {
        return output;
    }

    private void addInput(ObservableValue value) throws NodeException {
        output.checkBits(value);
        inputs.add(value);
        value.addListener(this);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        for (ObservableValue v : inputs)
            addInput(v);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    public static class FanInDescription extends PartTypeDescription {
        public FanInDescription(Class<?> clazz) {
            super(clazz);
            addAttributes();
        }

        public FanInDescription(String name, PartFactory partFactory) {
            super(name, partFactory);
            addAttributes();
        }

        private void addAttributes() {
            addAttribute(AttributeKey.Bits);
            addAttribute(AttributeKey.InputCount);
        }

        @Override
        public String[] getInputNames(PartAttributes partAttributes) {
            int count = partAttributes.get(AttributeKey.InputCount);
            String[] names = new String[count];
            for (int i = 0; i < count; i++)
                names[i] = "in_" + i;
            return names;
        }
    }
}
