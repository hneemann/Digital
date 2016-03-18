package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class Const implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(Const.class)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Value);
    private final ObservableValue output;

    public Const(PartAttributes attributes) {
        output = new ObservableValue("out", attributes.get(AttributeKey.Bits));
        output.setValue(attributes.get(AttributeKey.Value));
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException("no inputs available!");
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void registerNodes(Model model) {
    }
}
