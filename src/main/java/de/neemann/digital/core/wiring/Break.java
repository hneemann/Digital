package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Break implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Break.class, "brk")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Cycles);


    private ObservableValue input;

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        input = inputs[0].checkBits(1, null);
    }

    public ObservableValue getInput() {
        return input;
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[0];
    }

    @Override
    public void registerNodes(Model model) {
        model.addObserver(new MyModelStateObserver(model, this));
    }

    private static class MyModelStateObserver implements ModelStateObserver {
        private final Model model;
        private final Break aBreak;

        public MyModelStateObserver(Model model, Break aBreak) {
            this.model = model;
            this.aBreak = aBreak;
        }

        @Override
        public void handleEvent(ModelEvent event) {
            switch (event.getType()) {
                case FETCHBREAK:
                    event.registerBreak(aBreak);
                    break;
                case STARTED:
                    model.removeObserver(this);
                    break;
            }
        }
    }
}
