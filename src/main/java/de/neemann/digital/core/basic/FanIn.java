package de.neemann.digital.core.basic;

import de.neemann.digital.core.*;

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

    private void addInput(ObservableValue value) throws NodeException {
        output.checkBits(value);
        inputs.add(value);
        value.addListener(this);
    }

    public void removeInput(ObservableValue value) {
        inputs.remove(value);
        value.removeListener(this);
    }

    public ObservableValue getOutput() {
        return output;
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


    public abstract static class FanInFactory extends PartFactory {
        public FanInFactory(int count) {
            super(createNames(count));
        }

        private static String[] createNames(int count) {
            String[] names = new String[count];
            for (int i = 0; i < count; i++)
                names[i] = "in_" + i;
            return names;
        }
    }
}
