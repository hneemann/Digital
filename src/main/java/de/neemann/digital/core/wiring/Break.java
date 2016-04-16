package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Break element
 *
 * @author hneemann
 */
public class Break implements Element {

    /**
     * The Break description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Break.class, input("brk"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.CYCLES);

    private final int cycles;
    private ObservableValue input;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Break(ElementAttributes attributes) {
        cycles = attributes.get(Keys.CYCLES);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        input = inputs[0].checkBits(1, null);
    }

    /**
     * @return the break value
     */
    public ObservableValue getBreakInput() {
        return input;
    }

    /**
     * @return the timeout cycles
     */
    public int getCycles() {
        return cycles;
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[0];
    }

    @Override
    public void registerNodes(Model model) {
        model.addBreak(this);
    }

}
