package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * The Reset element
 *
 * @author hneemann
 */
public class Reset implements Element {

    /**
     * The Reset description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Reset", Reset.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private final ObservableValue output;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Reset(ElementAttributes attributes) {
        output = new ObservableValue("Reset", 1);
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
        model.addReset(this);
    }

    /**
     * @return the reset output
     */
    public ObservableValue getResetOutput() {
        return output;
    }

}
