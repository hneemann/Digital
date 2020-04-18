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
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The light bulb shape
 */
public class FuseShape implements Shape {
    private static final int BEZ = Math.round(SIZE2 / 2 * DILShape.CIRC);

    private static final Polygon OK_POLY = new Polygon(false)
            .add(0, 0)
            .add(new Vector(0, -BEZ), new Vector(SIZE2 / 2 - BEZ, -SIZE2 / 2), new Vector(SIZE2 / 2, -SIZE2 / 2))
            .add(new Vector(SIZE2 / 2 + BEZ, -SIZE2 / 2), new Vector(SIZE2, -SIZE2 / 2 + BEZ), new Vector(SIZE2, 0))
            .add(new Vector(SIZE2, BEZ), new Vector(SIZE2 + SIZE2 / 2 - BEZ, SIZE2 / 2), new Vector(SIZE2 + SIZE2 / 2, SIZE2 / 2))
            .add(new Vector(SIZE2 + SIZE2 / 2 + BEZ, SIZE2 / 2), new Vector(SIZE, SIZE2 / 2 - BEZ), new Vector(SIZE, 0));
    private static final Polygon BLOWN_POLY1 = new Polygon(false)
            .add(0, 0)
            .add(new Vector(0, -BEZ), new Vector(SIZE2 / 2 - BEZ, -SIZE2 / 2), new Vector(SIZE2 / 2, -SIZE2 / 2));
    private static final Polygon BLOWN_POLY2 = new Polygon(false)
            .add(SIZE2 + SIZE2 / 2, SIZE2 / 2)
            .add(new Vector(SIZE2 + SIZE2 / 2 + BEZ, SIZE2 / 2), new Vector(SIZE, SIZE2 / 2 - BEZ), new Vector(SIZE, 0));

    private final PinDescriptions outputs;
    private final boolean blown;


    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public FuseShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        blown = attr.get(Keys.BLOWN);
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, 0), outputs.get(0)))
                .add(new Pin(new Vector(SIZE, 0), outputs.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        if (blown) {
            graphic.drawPolygon(BLOWN_POLY1, Style.THIN);
            graphic.drawPolygon(BLOWN_POLY2, Style.THIN);
        } else
            graphic.drawPolygon(OK_POLY, Style.THIN);
    }
}
