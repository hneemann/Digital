/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 Or Shape
 */
public class IEEEOrShape extends IEEEGenericShape {

    private static final Polygon POLYGON = createPoly();
    private static final Polygon POLYGON_WIDE = createPolyWide();

    private static Polygon createPoly() {
        return new Polygon(true)
                .add(SIZE2, SIZE * 2 + SIZE2)
                .add(0, SIZE * 2 + SIZE2)
                .add(new Vector(SIZE2, SIZE * 2),
                        new Vector(SIZE2, 0),
                        new Vector(0, -SIZE2))
                .add(SIZE2, -SIZE2)
                .add(new Vector(SIZE, -SIZE2),
                        new Vector(SIZE * 2, 0),
                        new Vector(SIZE * 3, SIZE))
                .add(new Vector(SIZE * 2, SIZE * 2),
                        new Vector(SIZE, SIZE * 2 + SIZE2),
                        new Vector(SIZE2, SIZE * 2 + SIZE2));
    }

    private static Polygon createPolyWide() {
        return new Polygon(true)
                .add(SIZE + SIZE2, SIZE * 2 + SIZE2)
                .add(0, SIZE * 2 + SIZE2)
                .add(new Vector(SIZE2, SIZE2 * 3 + 4),
                        new Vector(SIZE2, 6),
                        new Vector(0, -SIZE2))
                .add(SIZE, -SIZE2)
                .add(new Vector(SIZE * 2 + SIZE2, -SIZE2),
                        new Vector(SIZE * 3 + SIZE2, 0),
                        new Vector(SIZE * 4, SIZE))
                .add(new Vector(SIZE * 3 + SIZE2, SIZE * 2),
                        new Vector(SIZE * 2 + SIZE2, SIZE * 2 + SIZE2),
                        new Vector(SIZE + SIZE2, SIZE * 2 + SIZE2));
    }

    private final boolean center;

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if NOr
     * @param attr    the elements attributes
     */
    public IEEEOrShape(PinDescriptions inputs, PinDescriptions outputs, boolean invert, ElementAttributes attr) {
        super(inputs, outputs, invert, attr);
        center = (inputs.size() & 1) != 0;
    }

    @Override
    protected void drawIEEE(Graphic graphic) {
        graphic.drawLine(new Vector(0, 0), new Vector(4, 0), Style.WIRE);
        graphic.drawLine(new Vector(0, SIZE * 2), new Vector(4, SIZE * 2), Style.WIRE);
        if (center)
            graphic.drawLine(new Vector(0, SIZE), new Vector(7, SIZE), Style.WIRE);
        if (isWideShape())
            graphic.drawPolygon(POLYGON_WIDE, Style.NORMAL);
        else
            graphic.drawPolygon(POLYGON, Style.NORMAL);
    }
}
