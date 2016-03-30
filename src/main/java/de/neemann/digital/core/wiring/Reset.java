package de.neemann.digital.core.wiring;

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
public class Reset implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Reset", Reset.class)
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label);

    private final ObservableValue output;
    private final String label;

    public Reset(ElementAttributes attributes) {
        output = new ObservableValue("Reset", 1);
        label = attributes.get(AttributeKey.Label);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"), null);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void registerNodes(Model model) {
        model.addReset(this);
    }

    public ObservableValue getResetOutput() {
        return output;
    }

}
