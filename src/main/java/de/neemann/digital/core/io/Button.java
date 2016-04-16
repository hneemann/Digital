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
public class Button implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Button.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private final ObservableValue output;
    private final String label;

    public Button(ElementAttributes attributes) {
        output = new ObservableValue("out", 1);
        label = attributes.get(Keys.LABEL);
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
