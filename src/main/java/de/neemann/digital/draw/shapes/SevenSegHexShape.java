/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * A seven segment shape with hex input
 */
public class SevenSegHexShape extends SevenShape {
    private static final int[] TABLE = new int[]{0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07, 0x7f, 0x6f, 0x77, 0x7c, 0x39, 0x5e, 0x79, 0x71};
    private final PinDescriptions inputs;
    private Pins pins;
    private Value input;
    private Value dp;
    private IOState ioState;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public SevenSegHexShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        super(attr);
        this.inputs = inputs;
    }

    @Override
    public void readObservableValues() {
        if (ioState != null) {
            input = ioState.getInput(0).getCopy();
            dp = ioState.getInput(1).getCopy();
        }
    }

    @Override
    protected boolean getStyle(int i) {
        if (input == null)
            return true;

        if (i == 7) {
            return dp.getBool();
        } else {
            if (input.isHighZ())
                return false;
            int v = (int) input.getValue() & 0xf;
            v = TABLE[v];
            return (v & (1 << i)) != 0;
        }
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins()
                    .add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), inputs.get(0)))
                    .add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), inputs.get(1)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        this.ioState = ioState;
        return null;
    }
}
