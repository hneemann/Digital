package de.neemann.digital.core.basic;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.*;

import java.util.ArrayList;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public abstract class FanIn extends Node implements Element {

    protected final ArrayList<ObservableValue> inputs;
    protected final ObservableValue output;
    private final int bits;

    public FanIn(int bits) {
        this.bits = bits;
        inputs = new ArrayList<>();
        output = new ObservableValue("out", bits);
    }

    public ObservableValue getOutput() {
        return output;
    }

    @Override
    public void setInputs(ObservableValue... in) throws NodeException {
        for (ObservableValue v : in)
            inputs.add(v.checkBits(bits, this).addObserverToValue(this));
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    public static class FanInDescription extends ElementTypeDescription {
        public FanInDescription(Class<? extends Element> clazz) {
            super(clazz);
            addAttributes();
        }

        public FanInDescription(String name, ElementFactory elementFactory) {
            super(name, elementFactory);
            addAttributes();
        }

        private void addAttributes() {
            addAttribute(AttributeKey.Rotate);
            addAttribute(AttributeKey.Bits);
            addAttribute(AttributeKey.InputCount);
        }

        @Override
        public PinDescription[] getInputDescription(ElementAttributes elementAttributes) {
            int count = elementAttributes.get(AttributeKey.InputCount);
            PinDescription[] names = new PinDescription[count];
            for (int i = 0; i < count; i++)
                names[i] = input("in_" + i);
            return names;
        }
    }
}
