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
            if (d.length() > 0)
                return d;
            else
                return super.getDescription(elementAttributes);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INPUT_DEFAULT)
            .addAttribute(Keys.IS_HIGH_Z)
            .addAttribute(Keys.DESCRIPTION)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.PINNUMBER);

    private final ObservableValue output;
    private final String label;
    private final String pinNumber;
    private final IntFormat format;
    private Model model;

    /**
     * Create a new instance
     *
     * @param attributes the inputs attributes
     */
    public In(ElementAttributes attributes) {
        InValue value = attributes.get(Keys.INPUT_DEFAULT);
        boolean highZ = attributes.get(Keys.IS_HIGH_Z) || value.isHighZ();
        pinNumber = attributes.get(Keys.PINNUMBER);
        output = new ObservableValue("out", attributes.get(Keys.BITS), highZ).setPinDescription(DESCRIPTION).setPinNumber(pinNumber);
        output.set(value.getValue(), value.isHighZ());
        label = attributes.getCleanLabel();
        format = attributes.get(Keys.INT_FORMAT);
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
        model.addInput(new Signal(label, output, output::set)
                .setPinNumber(pinNumber)
                .setFormat(format));
        this.model = model;
    }

    /**
     * @return the model this input is attached to
     */
    public Model getModel() {
        return model;
    }
}
