package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Const implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Const.class)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Value);

    private final ObservableValue output;

    public Const(ElementAttributes attributes) {
        output = new ObservableValue("out", attributes.get(AttributeKey.Bits));
        output.setValue(attributes.get(AttributeKey.Value));
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException("no inputs available!", null);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void registerNodes(Model model) {
    }
}
