package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;


/**
 * @author hneemann
 */
public class Decoder extends Node implements Element {

    private final int selectorBits;
    private final long defaultValue;
    private final ObservableValue[] output;
    private ObservableValue selector;

    private int oldSelectorValue;
    private int selectorValue;

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Decoder.class,
            input("sel", Lang.get("elem_Decode_pin_sel")))
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.SelectorBits)
            .addAttribute(AttributeKey.FlipSelPositon)
            .addAttribute(AttributeKey.Default);

    public Decoder(ElementAttributes attributes) {
        this.selectorBits = attributes.get(AttributeKey.SelectorBits);
        this.defaultValue = attributes.get(AttributeKey.Default);
        int outputs = 1 << selectorBits;
        output = new ObservableValue[outputs];
        for (int i = 0; i < outputs; i++) {
            output[i] = new ObservableValue("out_" + i, 1);
            output[i].setValue(defaultValue);
        }
    }

    @Override
    public ObservableValue[] getOutputs() {
        return output;
    }

    @Override
    public void readInputs() throws NodeException {
        selectorValue = (int) selector.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output[oldSelectorValue].setValue(defaultValue);
        output[selectorValue].setValue(1);
        oldSelectorValue = selectorValue;
    }

    public void setInputs(ObservableValue... inputs) throws NodeException {
        selector = inputs[0].addObserverToValue(this).checkBits(selectorBits, this);
    }

}
