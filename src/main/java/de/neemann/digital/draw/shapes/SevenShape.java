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
            Polygon.createFromPath("m 9,5 L 55,5 60,11 55,18 8,18 4,12 z"), // A
            Polygon.createFromPath("m 53,64 L 55,18 60,11 64,18 62,63 57,70 z"), // B
            Polygon.createFromPath("m 50,122 L 52,77 57,70 61,76 59,122 54,128 z"), // C
            Polygon.createFromPath("m 3,122 L 50,122 54,128 49,135 3,135 -1,129 z"), // D
            Polygon.createFromPath("m -5,122 L -3,76 1,70 5,77 3,122 -1,129 z"), // E
            Polygon.createFromPath("m -2,63 L 0,18 4,12 8,18 6,64 1,70 z"),  // F
            Polygon.createFromPath("m 6,64 L 53,64 57,70 52,77 5,77 1,70 z"), // G
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
