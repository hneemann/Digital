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
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.ACTIVE_LOW);

    private final ObservableValue output;
    private final String label;
    private final Boolean invert;
    private boolean pressed;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Button(ElementAttributes attributes) {
        output = new ObservableValue("out", 1).setPinDescription(DESCRIPTION);
        label = attributes.get(Keys.LABEL);
        invert = attributes.get(Keys.ACTIVE_LOW);
        output.setValue(invert ? 1 : 0);
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

    /**
     * Sets the buttons state
     *
     * @param pressed true if pressed
     */
    public void setPressed(boolean pressed) {
        if (pressed != this.pressed) {
            this.pressed = pressed;
            if (pressed ^ invert) {
                output.setValue(1);
            } else {
                output.setValue(0);
            }
        }
    }

    /**
     * @return true if pressed
     */
    public boolean isPressed() {
        return pressed;
    }
}
