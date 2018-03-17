/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.awt.*;

/**
 * The shape to show a seven seg display.
 * The state of the different segments is requested by calling {@link SevenShape#getStyle(int)}.
 */
public abstract class SevenShape implements Shape {

    static final int HEIGHT = 7;

    /**
     * the Frame of the display
     */
    public static final Polygon FRAME = Polygon.createFromPath("m -10,1 L 70,1 70,139 -10,139 z");
    private static final Polygon[] POLYGONS = new Polygon[]{
            Polygon.createFromPath("m 57,14 L 62,10 57,5 10,5 5,10 9,14 z"), // 0,
            Polygon.createFromPath("m 53,65 L 57,14 62,10 66,14 63,65 57,70 z"), // 1,
            Polygon.createFromPath("m 49,126 L 52,75 57,70 62,75 58,126 53,130 z"), // 2,
            Polygon.createFromPath("m 48,135 L 53,130 49,126 1,126 -3,130 1,135 z"), // 3,
            Polygon.createFromPath("m -7,126 L -4,75 1,70 5,75 1,126 -3,130 z"), // 4,
            Polygon.createFromPath("m -3,65 L 0,14 5,10 9,14 6,65 1,70 z"), // 5,
            Polygon.createFromPath("m 52,75 L 57,70 53,65 6,65 1,70 5,75 z"), // 6,
    };
    private static final Vector DOT = new Vector(58, 127);

    private final Style onStyle;
    private final Style offStyle;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public SevenShape(ElementAttributes attr) {
        onStyle = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
        offStyle = Style.NORMAL.deriveFillStyle(new Color(230, 230, 230));
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawPolygon(FRAME, Style.NORMAL);

        for (int i = 0; i < 7; i++)
            graphic.drawPolygon(POLYGONS[i], getStyleInt(i));

        graphic.drawCircle(DOT, DOT.add(8, 8), getStyleInt(7));
    }

    private Style getStyleInt(int i) {
        if (getStyle(i))
            return onStyle;
        else
            return offStyle;
    }

    /**
     * Returns the state of the segment
     *
     * @param i the segments number
     * @return true if activated
     */
    protected abstract boolean getStyle(int i);

}
