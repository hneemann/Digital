package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;


/**
 * @author hneemann
 */
public class Demultiplexer extends Node implements Element {

    private final int selectorBits;
    private final Integer bits;
    private final long defaultValue;
    private final ObservableValue[] output;
    private ObservableValue selector;
    private ObservableValue input;

    private int oldSelectorValue;
    private int selectorValue;
    private long value;

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Demultiplexer.class,
            input("sel", Lang.get("elem_Demultiplexer_pin_sel")),
            input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.SELECTOR_BITS)
            .addAttribute(Keys.FLIP_SEL_POSITON)
            .addAttribute(Keys.DEFAULT);

    public Demultiplexer(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        this.selectorBits = attributes.get(Keys.SELECTOR_BITS);
        this.defaultValue = attributes.get(Keys.DEFAULT);
        int outputs = 1 << selectorBits;
        output = new ObservableValue[outputs];
        for (int i = 0; i < outputs; i++) {
            output[i] = new ObservableValue("out_" + i, bits);
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
        value = input.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output[oldSelectorValue].setValue(defaultValue);
        output[selectorValue].setValue(value);
        oldSelectorValue = selectorValue;
    }

    public void setInputs(ObservableValue... inputs) throws NodeException {
        selector = inputs[0].addObserverToValue(this).checkBits(selectorBits, this);
        input = inputs[1].addObserverToValue(this).checkBits(bits, this);
    }

}
