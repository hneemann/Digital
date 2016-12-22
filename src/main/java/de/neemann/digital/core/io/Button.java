package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * The Button
 *
 * @author hneemann
 */
public class Button implements Element {

    /**
     * The Button description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Button.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private final ObservableValue output;
    private final String label;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Button(ElementAttributes attributes) {
        output = new ObservableValue("out", 1).setPinDescription(DESCRIPTION);
        label = attributes.get(Keys.LABEL);
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
        model.addSignal(new Signal(label, output));
    }
}
