/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A Monoflop
 */
public class Monoflop extends FlipflopBit {

    /**
     * The Monoflop's description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Monoflop.class,
            input("C").setClock(),
            input("R"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.MONOFLOP_DELAY)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final int delayTime;
    private ObservableValue clock;
    private ObservableValue rst;
    private boolean lastClock;
    private int counter;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public Monoflop(ElementAttributes attr) {
        super(attr, DESCRIPTION);
        delayTime = attr.get(Keys.MONOFLOP_DELAY);
        if (isOut())
            counter = delayTime;
        else
            counter = 0;
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clockVal = clock.getBool();
        if (rst.getBool())
            setOut(false);
        else if (clockVal && !lastClock) {
            counter = delayTime;
            setOut(true);
        }
        lastClock = clockVal;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        clock = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        rst = inputs.get(1).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public void init(Model model) throws NodeException {
        ArrayList<Clock> clockList = model.getClocks();
        if (clockList.size() != 1)
            throw new NodeException(Lang.get("err_monoflopRequiresOneClock"));

        final ObservableValue clock = clockList.get(0).getClockOutput();
        clock.addObserver(() -> {
            if (clock.getBool()) {
                if (counter > 0) {
                    counter--;
                    if (counter == 0) {
                        setOut(false);
                        Monoflop.this.hasChanged();
                    }
                }
            }
        });
    }
}
