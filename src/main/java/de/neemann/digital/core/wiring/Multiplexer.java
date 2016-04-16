package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.FanIn;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

import java.util.Arrays;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class Multiplexer extends FanIn {

    private final int selectorBits;
    private ObservableValue selector;
    private long value;

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Multiplexer.class) {
        @Override
        public PinDescription[] getInputDescription(ElementAttributes elementAttributes) {
            int size = 1 << elementAttributes.get(AttributeKey.SelectorBits);
            PinDescription[] names = new PinDescription[size + 1];
            names[0] = input("sel", Lang.get("elem_Multiplexer_pin_sel"));
            for (int i = 0; i < size; i++)
                names[i + 1] = input("in_" + i);
            return names;
        }
    }
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.FlipSelPositon)
            .addAttribute(AttributeKey.SelectorBits);

    public Multiplexer(ElementAttributes attributes) {
        super(attributes.get(AttributeKey.Bits));
        this.selectorBits = attributes.get(AttributeKey.SelectorBits);
    }

    @Override
    public void readInputs() throws NodeException {
        int n = (int) selector.getValue();
        value = getInputs().get(n).getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        getOutput().setValue(value);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        selector = inputs[0].addObserverToValue(this).checkBits(selectorBits, this);
        ObservableValue[] in = Arrays.copyOfRange(inputs, 1, inputs.length);
        super.setInputs(in);

        if (in.length != (1 << selectorBits))
            throw new BitsException(Lang.get("err_selectorInputCountMismatch"), this, selector);
    }
}
