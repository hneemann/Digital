/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.ieee;

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
import de.neemann.digital.draw.shapes.Interactor;
import de.neemann.digital.draw.shapes.Shape;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 Not Shape
 */
public class IEEENotShape implements Shape {
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final boolean wideShape;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param inputs     the inputs
     * @param outputs    the outputs
     * @param attributes the elements attributes
     */
    public IEEENotShape(PinDescriptions inputs, PinDescriptions outputs, ElementAttributes attributes) {
        this.inputs = inputs;
        this.outputs = outputs;
        wideShape = attributes.get(Keys.WIDE_SHAPE);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(0, 0), inputs.get(0)));
            int width = SIZE * 2;
            if (wideShape)
                width += SIZE;
            pins.add(new Pin(new Vector(width, 0), outputs.get(0)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        if (wideShape) {
            graphic.drawPolygon(
                    new Polygon(true)
                            .add(1, -SIZE - 2)
                            .add(SIZE * 2 - 1, 0)
                            .add(1, SIZE + 2), Style.NORMAL
            );
            graphic.drawCircle(new Vector(SIZE * 2 + 1, -SIZE2 + 1),
                    new Vector(SIZE * 3 - 1, SIZE2 - 1), Style.NORMAL);
        } else {
            graphic.drawPolygon(
                    new Polygon(true)
                            .add(1, -SIZE2 - 2)
                            .add(SIZE - 1, 0)
                            .add(1, SIZE2 + 2), Style.NORMAL
            );
            graphic.drawCircle(new Vector(SIZE + 1, -SIZE2 + 1),
                    new Vector(SIZE * 2 - 1, SIZE2 - 1), Style.NORMAL);
        }
    }
}
