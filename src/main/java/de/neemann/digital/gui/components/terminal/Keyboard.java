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

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Keyboard component
 */
public class Keyboard extends Node implements Element {

    /**
     * The keyboard description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Keyboard.class,
            input("C").setClock(),
            input("en"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERTER_CONFIG);

    private final String label;
    private final ObservableValue data;
    private final ObservableValue keyAvail;
    private KeyboardInterface keyboardInterface;
    private ObservableValue clock;
    private ObservableValue enable;
    private boolean enableVal;
    private boolean lastClock = false;

    /**
     * Creates a new terminal instance
     *
     * @param attributes the attributes
     */
    public Keyboard(ElementAttributes attributes) {
        data = new ObservableValue("D", 16)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
        keyAvail = new ObservableValue("av", 1)
                .setPinDescription(DESCRIPTION);
        label = attributes.getLabel();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        clock = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
        enable = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(data, keyAvail);
    }

    @Override
    public void readInputs() throws NodeException {
        enableVal = enable.getBool();
        boolean nowClock = clock.getBool();

        if (keyboardInterface != null && nowClock && !lastClock && enableVal)
            keyboardInterface.deleteFirstChar();

        lastClock = nowClock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (keyboardInterface != null) {
            if (enableVal)
                data.setValue(keyboardInterface.getChar());
            else
                data.setToHighZ();
            keyAvail.setBool(keyboardInterface.getChar() != 0);
        } else {
            if (enableVal)
                data.setValue(0);
            else
                data.setToHighZ();
            keyAvail.setBool(false);
        }
    }

    /**
     * Sets the keyboard interface
     *
     * @param keyboardInterface the keyboard interface
     */
    public void setKeyboard(KeyboardInterface keyboardInterface) {
        this.keyboardInterface = keyboardInterface;
    }

    /**
     * @return the keyboard label
     */
    public String getLabel() {
        return label;
    }

    /**
     * The keyboard interface
     */
    public interface KeyboardInterface {
        /**
         * @return a char or 0 if no char available, does not remove the char
         */
        int getChar();

        /**
         * removes the first char
         */
        void deleteFirstChar();

    }
}
