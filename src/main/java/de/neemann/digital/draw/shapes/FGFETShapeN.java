/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The n-chan floating gate FET shape
 */
public class FGFETShapeN extends FETShape {
    static final Style CHARGED_GATE = Style.NORMAL.deriveStyle(6, false, Color.RED);

    private final boolean programmed;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public FGFETShapeN(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        super(attributes, inputs, outputs);
        programmed = attributes.get(Keys.BLOWN);
        setXOffs(SIZE2 + 1);
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, SIZE * 2), getInputs().get(0)))
                .add(new Pin(new Vector(SIZE, 0), getOutputs().get(0)))
                .add(new Pin(new Vector(SIZE, SIZE * 2), getOutputs().get(1)));
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        super.drawTo(graphic, highLight);

        if (programmed)
            graphic.drawLine(new Vector(6, 2 * SIZE - 4), new Vector(6, 4), CHARGED_GATE);
        else
            graphic.drawLine(new Vector(6, 2 * SIZE - 4), new Vector(6, 4), Style.THIN);

        // the arrow
        graphic.drawLine(new Vector(SIZE2 + 8, SIZE), new Vector(SIZE + 3, SIZE), Style.THIN);
        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2 + 5, SIZE)
                .add(SIZE - SIZE2 / 3 + 3, SIZE - SIZE2 / 4)
                .add(SIZE - SIZE2 / 3 + 3, SIZE + SIZE2 / 4), Style.THIN_FILLED);
    }
}
