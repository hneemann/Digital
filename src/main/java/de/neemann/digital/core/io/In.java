package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * The Input
 *
 * @author hneemann
 */
public class In implements Element {

    /**
     * The inputs description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(In.class) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            String d = elementAttributes.get(Keys.DESCRIPTION);
            if (d.length()>0)
                return d;
            else
                return super.getDescription(elementAttributes);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.IS_HIGH_Z)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DESCRIPTION)
            .addAttribute(Keys.DEFAULT);

    private final ObservableValue output;
    private final String label;
    private final String description;

    /**
     * Create a new instance
     *
     * @param attributes the inputs attributes
     */
    public In(ElementAttributes attributes) {
        boolean highZ = attributes.get(Keys.IS_HIGH_Z);
        output = new ObservableValue("out", attributes.get(Keys.BITS), highZ);
        output.setValue(attributes.get(Keys.DEFAULT));
        label = attributes.getCleanLabel();
        description = attributes.get(Keys.DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
        model.addInput(new Signal(label, output).setDescription(description));
    }
}
