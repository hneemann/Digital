/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 */
public class Clock implements Element {

    /**
     * the clocks description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Clock", Clock.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.RUN_AT_REAL_TIME)
            .addAttribute(Keys.FREQUENCY)
            .addAttribute(Keys.PINNUMBER)
            .supportsHDL();

    private final ObservableValue output;
    private final int frequency;
    private final String label;
    private final String clockPin;

    /**
     * Creates a new instance
     *
     * @param attributes the clocks attributes
     */
    public Clock(ElementAttributes attributes) {
        output = new ObservableValue("C", 1).setPinDescription(DESCRIPTION);
        if (attributes.get(Keys.RUN_AT_REAL_TIME)) {
            int f = attributes.get(Keys.FREQUENCY);
            if (f < 1) f = 1;
            frequency = f;
        } else
            frequency = 0;
        label = attributes.getLabel();
        clockPin = attributes.get(Keys.PINNUMBER);
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
        model.addClock(this);
        model.addSignal(new Signal(label, output));
    }

    /**
     * @return the clock output value
     */
    public ObservableValue getClockOutput() {
        return output;
    }

    /**
     * @return the clocks frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @return the clocks label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the clock pin
     */
    public String getClockPin() {
        return clockPin;
    }
}
