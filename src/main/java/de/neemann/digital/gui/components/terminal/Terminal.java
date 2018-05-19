/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import javax.swing.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 */
public class Terminal extends Node implements Element {

    /**
     * The terminal description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Terminal.class,
            input("D"),
            input("C").setClock(),
            input("en"))
            .addAttribute(Keys.TERM_WIDTH)
            .addAttribute(Keys.TERM_HEIGHT)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private final String label;
    private final ElementAttributes attr;
    private TerminalDialog terminalDialog;
    private ObservableValue data;
    private ObservableValue clock;
    private boolean lastClock;
    private ObservableValue en;

    /**
     * Creates a new terminal instance
     *
     * @param attributes the attributes
     */
    public Terminal(ElementAttributes attributes) {
        label = attributes.getCleanLabel();
        attr = attributes;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        data = inputs.get(0);
        clock = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        en = inputs.get(2).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clockVal = clock.getBool();
        if (!lastClock && clockVal && en.getBool()) {
            long value = data.getValue();
            if (value != 0)
                SwingUtilities.invokeLater(() -> {
                    if (terminalDialog == null || !terminalDialog.isVisible()) {
                        terminalDialog = new TerminalDialog(getModel().getWindowPosManager().getMainFrame(), attr);
                        getModel().getWindowPosManager().register("terminal_" + label, terminalDialog);
                    }
                    terminalDialog.addChar((char) value);
                });
        }
        lastClock = clockVal;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

}
