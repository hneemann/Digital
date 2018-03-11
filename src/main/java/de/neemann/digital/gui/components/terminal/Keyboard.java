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
public class Keyboard extends Node implements Element {

    /**
     * The keyboard description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Keyboard.class,
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private final String label;
    private KeyboardDialog keyboardDialog;
    private ObservableValue data;
    private ObservableValue select;
    private boolean sel;
    private boolean lastSel = false;
    private int keyData;

    /**
     * Creates a new terminal instance
     *
     * @param attributes the attributes
     */
    public Keyboard(ElementAttributes attributes) {
        data = new ObservableValue("D", 16)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
        label = attributes.getCleanLabel();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        select = inputs.get(0).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return data.asList();
    }

    @Override
    public void readInputs() throws NodeException {
        sel = select.getBool();
        if (!lastSel && sel) {
            KeyboardDialog kbd = getKeyboard();
            if (kbd != null)
                keyData = kbd.getChar();
            else
                keyData = 0;
        }
        lastSel = sel;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (sel)
            data.setValue(keyData);
        else
            data.setToHighZ();
    }

    private KeyboardDialog getKeyboard() {
        if (keyboardDialog == null || !keyboardDialog.isVisible()) {
            SwingUtilities.invokeLater(() -> {
                if (keyboardDialog == null || !keyboardDialog.isVisible()) {
                    keyboardDialog = new KeyboardDialog(getModel().getWindowPosManager().getMainFrame());
                    getModel().getWindowPosManager().register("keyboard_" + label, keyboardDialog);
                }
            });
        }
        return keyboardDialog;
    }
}
