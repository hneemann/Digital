package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class In implements Element {

    /**
     * The inputs description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(In.class) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            return elementAttributes.get(Keys.Description);
        }
    }
            .addAttribute(Keys.Rotate)
            .addAttribute(Keys.IsHighZ)
            .addAttribute(Keys.Bits)
            .addAttribute(Keys.Label)
            .addAttribute(Keys.Description)
            .addAttribute(Keys.Default);

    private final ObservableValue output;
    private final String label;

    /**
     * Create a new instance
     *
     * @param attributes the inputs attributes
     */
    public In(ElementAttributes attributes) {
        boolean highZ = attributes.get(Keys.IsHighZ);
        output = new ObservableValue("out", attributes.get(Keys.Bits), highZ);
        output.setValue(attributes.get(Keys.Default));
        label = attributes.get(Keys.Label);
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
