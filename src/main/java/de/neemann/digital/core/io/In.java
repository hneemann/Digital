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
public class In implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(In.class).addAttribute(AttributeKey.Bits);
    private final ObservableValue output;

    public In(PartAttributes attributes) {
        output = new ObservableValue("out", attributes.get(AttributeKey.Bits));
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
