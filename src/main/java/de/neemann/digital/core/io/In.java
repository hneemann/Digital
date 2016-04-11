package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class In implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(In.class)
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.IsHighZ)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Default);

    private final ObservableValue output;
    private final String label;
    private final boolean highZ;

    public In(ElementAttributes attributes) {
        highZ = attributes.get(AttributeKey.IsHighZ);
        output = new ObservableValue("out", attributes.get(AttributeKey.Bits), highZ);
        output.setValue(attributes.get(AttributeKey.Default));
        label = attributes.get(AttributeKey.Label);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void registerNodes(Model model) {
        model.addSignal(label, output);
    }
}
