package de.neemann.digital.core.pld;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;

/**
 * Only a placeholder.
 * Has no connections to the model!
 *
 * @author hneemann
 */
public class PullDown implements Element {

    /**
     * The pull down description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("PullDown", PullDown.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS);


    private final ObservableValue output;

    /**
     * Creates a new pull down element
     *
     * @param attr the attributes
     */
    public PullDown(ElementAttributes attr) {
        int bits = attr.getBits();
        output = new PullObservableValue(bits, PinDescription.PullResistor.pullDown).setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
    }

    /**
     * Value that represents a pull resistor
     */
    public static final class PullObservableValue extends ObservableValue {
        private final PullResistor res;

        PullObservableValue(int bits, PullResistor res) {
            super("out", bits, true);
            this.res = res;
        }

        @Override
        public PullResistor getPullResistor() {
            return res;
        }
    }
}
