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

/**
 * A pull down resistor shape
 */
public class PullDownShape implements Shape {
    /**
     * half the width of the resistor
     */
    public static final int WIDTH2 = SIZE2 - 3;
    /**
     * height of the resistor
     */
    public static final int HEIGHT = SIZE + SIZE / 3;


    private final PinDescriptions outputs;

    /**
     * Creates a new instance
     *
     * @param attributes attributes
     * @param inputs     inputs
     * @param outputs    outputs
     */
    public PullDownShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
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
                        .add(-WIDTH2, 1)
                        .add(-WIDTH2, HEIGHT)
                        .add(WIDTH2, HEIGHT)
                        .add(WIDTH2, 1),
                Style.NORMAL
        );
        graphic.drawLine(new Vector(0, HEIGHT), new Vector(0, SIZE * 2), Style.NORMAL);
        graphic.drawLine(new Vector(-SIZE2, SIZE * 2), new Vector(SIZE2, SIZE * 2), Style.THICK);
    }

}
