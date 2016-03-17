package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.gui.draw.shapes.InputShape;

/**
 * @author hneemann
 */
public class In implements Part {

    private final ObservableValue output;

    public In(int bits) {
        output = new ObservableValue("out", bits);
    }

    public static PartDescription createFactory(int bits) {
        return new PartDescription(new InputShape(bits), () -> new In(bits));
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException("no inputs available!");
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void registerNodes(Model model) {
    }
}
