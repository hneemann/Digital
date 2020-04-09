/*
 * Copyright (c) 2016 Helmut Neemann
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
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The driver shape
 */
public class DriverShape implements Shape {
    private final boolean bottom;
    private final boolean invertedInput;
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
    public DriverShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this(attr, inputs, outputs, false);
    }

    /**
     * Creates a new instance
     *
     * @param attr          the attributes
     * @param inputs        the inputs
     * @param outputs       the outputs
     * @param invertedInput true if input is inverted
     */
    public DriverShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs, boolean invertedInput) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.bottom = attr.get(Keys.FLIP_SEL_POSITON);
        this.invertedInput = invertedInput;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(-SIZE, 0), inputs.get(0)));
            pins.add(new Pin(new Vector(0, bottom ? SIZE : -SIZE), inputs.get(1)));
            pins.add(new Pin(new Vector(SIZE, 0), outputs.get(0)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawPolygon(
                new Polygon(true)
                        .add(-SIZE + 1, -SIZE2 - 2)
                        .add(SIZE - 1, 0)
                        .add(-SIZE + 1, SIZE2 + 2), Style.NORMAL
        );
        if (bottom) {
            if (invertedInput)
                graphic.drawCircle(new Vector(-SIZE2 + 4, SIZE), new Vector(SIZE2 - 4, 8), Style.NORMAL);
            else
                graphic.drawLine(new Vector(0, SIZE), new Vector(0, 7), Style.NORMAL);
        } else {
            if (invertedInput)
                graphic.drawCircle(new Vector(-SIZE2 + 4, -SIZE), new Vector(SIZE2 - 4, -8), Style.NORMAL);
            else
                graphic.drawLine(new Vector(0, -SIZE), new Vector(0, -7), Style.NORMAL);
        }
    }
}
