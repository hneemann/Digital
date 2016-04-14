package de.neemann.digital.core.memory;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A look up table which can be used as a generic customizable gate.
 *
 * @author hneemann
 */
public class LookUpTable extends Node implements Element {

    /**
     * The LUTs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LookUpTable.class) {
        @Override
        public PinDescription[] getInputNames(ElementAttributes elementAttributes) {
            int size = elementAttributes.get(AttributeKey.InputCount);
            PinDescription[] names = new PinDescription[size];
            for (int i = 0; i < size; i++)
                names[i] = input("in_" + i);
            return names;
        }
    }
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.InputCount)
            .addAttribute(AttributeKey.Data)
            .setShortName("LUT");

    private final DataField data;
    private final ObservableValue output;
    private ObservableValue[] inputs;
    private int addr;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public LookUpTable(ElementAttributes attr) {
        int bits = attr.get(AttributeKey.Bits);
        output = new ObservableValue("out", bits);
        data = attr.get(AttributeKey.Data);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        this.inputs = inputs;
        for (int i = 0; i < inputs.length; i++)
            inputs[i].checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void readInputs() throws NodeException {
        addr = 0;
        int mask = 1;
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i].getValue() > 0)
                addr = addr | mask;
            mask = mask * 2;
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(data.getData(addr));
    }

}
