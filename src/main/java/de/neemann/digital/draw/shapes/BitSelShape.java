/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * The Muxer shape
 */
public class BitSelShape implements Shape {
    private final boolean flip;
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public BitSelShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.flip = attr.get(Keys.FLIP_SEL_POSITON);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins()
                    .add(new Pin(new Vector(SIZE, (flip ? -1 : 1) * SIZE), inputs.get(1)))
                    .add(new Pin(new Vector(0, 0), inputs.get(0)))
                    .add(new Pin(new Vector(SIZE * 2, 0), outputs.get(0)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(1, -SIZE-4)
                .add(SIZE * 2 - 1, -SIZE+5)
                .add(SIZE * 2 - 1, SIZE - 5)
                .add(1, SIZE + 4), Style.NORMAL);
    }
}
