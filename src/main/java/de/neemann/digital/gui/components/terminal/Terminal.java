package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Terminal extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Terminal.class, "D", "C")
            .addAttribute(AttributeKey.TermWidth)
            .addAttribute(AttributeKey.TermHeight)
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label);

    private final ElementAttributes attr;
    private ObservableValue data;
    private ObservableValue clock;
    private TerminalDialog terminalDialog;
    private boolean lastClock;

    public Terminal(ElementAttributes attributes) {
        attr = attributes;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        data = inputs[0];
        clock = inputs[1].addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[0];
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clockVal = clock.getBool();
        if (!lastClock && clockVal) {
            if (terminalDialog == null)
                terminalDialog = new TerminalDialog(attr);
            terminalDialog.addChar((char) data.getValue());
        }
        lastClock = clockVal;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

}
