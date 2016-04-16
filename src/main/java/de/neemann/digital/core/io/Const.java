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
public class Const implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Const.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.VALUE);

    private final ObservableValue output;

    public Const(ElementAttributes attributes) {
        output = new ObservableValue("out", attributes.get(Keys.BITS));
        output.setValue(attributes.get(Keys.VALUE));
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
    }
}
