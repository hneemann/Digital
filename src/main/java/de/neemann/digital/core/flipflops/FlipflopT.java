/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The T flip-flop
 */
public class FlipflopT extends FlipflopBit {

    /**
     * The T-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("T_FF", FlipflopT.class) {
        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
            if (elementAttributes.get(Keys.WITH_ENABLE))
                return new PinDescriptions(input("T"), input("C").setClock()).setLangKey(getPinLangKey());
            else
                return new PinDescriptions(input("C").setClock()).setLangKey(getPinLangKey());
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.WITH_ENABLE)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final boolean isEnable;

    private ObservableValue clockVal;
    private ObservableValue enable;
    private boolean lastClock;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopT(ElementAttributes attributes) {
        super(attributes, DESCRIPTION);
        isEnable = attributes.get(Keys.WITH_ENABLE);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            if (enable == null)
                setOut(!isOut());
            else {
                if (enable.getBool())
                    setOut(!isOut());
            }
        }
        lastClock = clock;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        if (isEnable) {
            enable = inputs.get(0).checkBits(1, this, 0);
            clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        } else
            clockVal = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
    }

    /**
     * @return the clock value
     */
    public ObservableValue getClockVal() {
        return clockVal;
    }

    /**
     * @return enable value or null if not available
     */
    public ObservableValue getEnableVal() {
        return enable;
    }

}
