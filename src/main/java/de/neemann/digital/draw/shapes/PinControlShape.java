/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The shape for the pin control logic
 */
public class PinControlShape implements Shape {
    private final PinDescriptions in;
    private final PinDescriptions out;
    private Pins pins;

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
        if (pins == null)
            pins = new Pins()
                    .add(new Pin(new Vector(0, 0), in.get(0)))
                    .add(new Pin(new Vector(SIZE, -SIZE), in.get(1)))
                    .add(new Pin(new Vector(SIZE * 2, SIZE), out.get(0)))
                    .add(new Pin(new Vector(SIZE * 3, 0), out.get(1)));
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawLine(new Vector(0, 0), new Vector(SIZE2, 0), Style.NORMAL);
        graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE * 3, 0), Style.NORMAL);
        graphic.drawLine(new Vector(SIZE * 2, 0), new Vector(SIZE * 2, SIZE), Style.NORMAL);
        graphic.drawPolygon(new Polygon()
                .add(SIZE2, SIZE2)
                .add(SIZE2, -SIZE2)
                .add(SIZE + SIZE2, 0), Style.NORMAL);
        graphic.drawLine(new Vector(SIZE, -SIZE), new Vector(SIZE, -6), Style.NORMAL);
    }
}
