/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
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
import static de.neemann.digital.draw.shapes.PullDownShape.HEIGHT;
import static de.neemann.digital.draw.shapes.PullDownShape.WIDTH2;
import static de.neemann.digital.draw.shapes.VDDShape.DOWNSHIFT;

/**
 * A pull up resistor shape.
 */
public class PullUpShape implements Shape {

    private final PinDescriptions outputs;

    /**
     * Creates a new instance
     *
     * @param attributes attributes
     * @param inputs     inputs
     * @param outputs    outputs
     */
    public PullUpShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawPolygon(
                new Polygon(true)
                        .add(-WIDTH2, -1)
                        .add(-WIDTH2, -HEIGHT)
                        .add(WIDTH2, -HEIGHT)
                        .add(WIDTH2, -1),
                Style.NORMAL
        );
        graphic.drawLine(new Vector(0, -HEIGHT), new Vector(0, DOWNSHIFT - SIZE * 2 - SIZE2), Style.NORMAL);
        graphic.drawPolygon(
                new Polygon(false)
                        .add(-SIZE2, DOWNSHIFT - SIZE * 2)
                        .add(0, DOWNSHIFT - SIZE * 2 - SIZE * 2 / 3)
                        .add(SIZE2, DOWNSHIFT - SIZE * 2),
                Style.NORMAL
        );
    }

}
