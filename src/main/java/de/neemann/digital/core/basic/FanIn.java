package de.neemann.digital.core.basic;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.*;

import java.util.ArrayList;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A fan in. Used as base class for the simple bool operations
 *
 * @author hneemann
 */
public abstract class FanIn extends Node implements Element {

    private final ArrayList<ObservableValue> inputs;
    private final ObservableValue output;
    private final int bits;

    /**
     * Creates a new instance
     *
     * @param bits the number of bits
     */
    public FanIn(int bits) {
        this.bits = bits;
        inputs = new ArrayList<>();
        output = new ObservableValue("out", bits);
    }

    @Override
    public void setInputs(ObservableValue... in) throws NodeException {
        for (ObservableValue v : in)
            inputs.add(v.checkBits(bits, this).addObserverToValue(this));
    }

    /**
     * @return the outputs
     */
    public ObservableValue getOutput() {
        return output;
    }

    /**
     * @return the outputs
     */
    public ArrayList<ObservableValue> getInputs() {
        return inputs;
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    /**
     * The fan in description
     */
    static class FanInDescription extends ElementTypeDescription {
        FanInDescription(Class<? extends Element> clazz) {
            super(clazz);
            addAttributes();
        }

        private void addAttributes() {
            addAttribute(Keys.Rotate);
            addAttribute(Keys.Bits);
            addAttribute(Keys.InputCount);
        }

        @Override
        public PinDescription[] getInputDescription(ElementAttributes elementAttributes) {
            int count = elementAttributes.get(Keys.InputCount);
            PinDescription[] names = new PinDescription[count];
            for (int i = 0; i < count; i++)
                names[i] = input("in_" + i);
            return names;
        }
    }
}
