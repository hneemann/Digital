package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class Delay extends Node implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(Delay.class, "in");

    private final ObservableValue output;
    private final int bits;
    private ObservableValue input;
    private long value;

    public Delay(PartAttributes attributes) {
        bits = attributes.get(AttributeKey.Bits);
        output = new ObservableValue("out", bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    public ObservableValue getOutput() {
        return output;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        input = inputs[0].addObserver(this);

        if (input.getBits() != bits)
            throw new BitsException("wrongBitCountInDelay", input, output);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

}
