package de.neemann.digital.core.basic;

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
public class Not extends Node implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(Not.class, "in");
    private final ObservableValue output;
    private ObservableValue input;
    private long value;

    public Not(PartAttributes attributes) {
        output = new ObservableValue("out", attributes.get(AttributeKey.Bits));
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(~value);
    }

    public ObservableValue getOutput() {
        return output;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        input = inputs[0].addObserver(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

}
