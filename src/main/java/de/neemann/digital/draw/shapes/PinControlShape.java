/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * The shape for the pin control logic
 */
public class PinControlShape implements Shape {
    private final PinDescriptions in;
    private final PinDescriptions out;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     * @param in   the inputs
     * @param out  the outputs
     */
    public PinControlShape(ElementAttributes attr, PinDescriptions in, PinDescriptions out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, -SIZE), in.get(0)))
                .add(new Pin(new Vector(0, 0), out.get(0)))
                .add(new Pin(new Vector(0, +SIZE), in.get(1)))
                .add(new Pin(new Vector(SIZE * 2, 0), out.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawLine(new Vector(0, -SIZE), new Vector(SIZE, 0), Style.NORMAL);
        graphic.drawLine(new Vector(0, 0), new Vector(SIZE * 2, 0), Style.NORMAL);
        graphic.drawLine(new Vector(0, SIZE), new Vector(SIZE, 0), Style.NORMAL);
    }
}
