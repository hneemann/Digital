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
 * IEEE Standard 91-1984 And Shape
 */
public class IEEEAndShape extends IEEEGenericShape {

    private static final Polygon POLYGON = createPoly();
    private static final Polygon POLYGON_WIDE = createPolyWide();

    private static Polygon createPoly() {
        return new Polygon(true)
                .add(SIZE + SIZE2, SIZE * 2 + SIZE2)
                .add(1, SIZE * 2 + SIZE2)
                .add(1, -SIZE2)
                .add(SIZE + SIZE2, -SIZE2)
                .add(new Vector(SIZE * 2, -SIZE2), new Vector(SIZE * 3, 0), new Vector(SIZE * 3 - 1, SIZE))
                .add(new Vector(SIZE * 3 - 1, SIZE * 2), new Vector(SIZE * 2, SIZE * 2 + SIZE2), new Vector(SIZE + SIZE2, SIZE * 2 + SIZE2));
    }

    private static Polygon createPolyWide() {
        return new Polygon(true)
                .add(SIZE * 2 + SIZE2, SIZE * 2 + SIZE2)
                .add(1, SIZE * 2 + SIZE2)
                .add(1, -SIZE2)
                .add(SIZE * 2 + SIZE2, -SIZE2)
                .add(new Vector(SIZE * 3, -SIZE2), new Vector(SIZE * 4, 0), new Vector(SIZE * 4 - 1, SIZE))
                .add(new Vector(SIZE * 4 - 1, SIZE * 2), new Vector(SIZE * 3, SIZE * 2 + SIZE2), new Vector(SIZE * 2 + SIZE2, SIZE * 2 + SIZE2));
    }

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if NAnd
     * @param attr    the attributes
     */
    public IEEEAndShape(PinDescriptions inputs, PinDescriptions outputs, boolean invert, ElementAttributes attr) {
        super(inputs, outputs, invert, attr);
    }

    @Override
    protected void drawIEEE(Graphic graphic) {
        if (isWideShape())
            graphic.drawPolygon(POLYGON_WIDE, Style.NORMAL);
        else
            graphic.drawPolygon(POLYGON, Style.NORMAL);
    }

}
