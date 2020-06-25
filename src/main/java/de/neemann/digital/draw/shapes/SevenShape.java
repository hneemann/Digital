/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.graphics.*;

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
            Polygon.createFromPath("m 12.04,5.0 L 52.8,5.0 58.15,10.61 52.28,16.22 11.52,16.22 6.17,10.61 z"), // 0,
            Polygon.createFromPath("m 59.49,12.01 L 64.84,17.62 62.74,62.99 56.87,68.6 51.52,62.99 53.62,17.62 z"), // 1,
            Polygon.createFromPath("m 56.74,71.4 L 62.09,77.01 60.0,122.38 54.13,127.99 48.78,122.38 50.88,77.01 z"), // 2,
            Polygon.createFromPath("m 6.55,123.78 L 47.32,123.78 52.67,129.39 46.8,135.0 6.04,135.0 0.69,129.39 z"), // 3,
            Polygon.createFromPath("m 1.96,71.4 L 7.31,77.01 5.22,122.38 -0.64,127.99 -5.99,122.38 -3.9,77.01 z"), // 4,
            Polygon.createFromPath("m 4.7,12.01 L 10.05,17.62 7.96,62.99 2.09,68.6 -3.25,62.99 -1.15,17.62 z"), // 5,
            Polygon.createFromPath("m 9.3,64.39 L 50.06,64.39 55.41,70.0 49.54,75.61 8.78,75.61 3.43,70.0 z"), // 6,
    };
    private static final Vector DOT = new Vector(58, 127);

    private final Style onStyle;
    private final Style offStyle;
    private final int size;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public SevenShape(ElementAttributes attr) {
        onStyle = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
        offStyle = Style.NORMAL.deriveFillStyle(ColorKey.GRID);
        size = attr.get(Keys.SEVEN_SEG_SIZE);
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        Transform tr = createTransform(size);
        graphic.drawPolygon(FRAME.transform(tr), Style.NORMAL);
        for (int i = 0; i < 7; i++)
            graphic.drawPolygon(POLYGONS[i].transform(tr), getStyleInt(i));

        graphic.drawCircle(DOT.transform(tr), DOT.add(8, 8).transform(tr), getStyleInt(7));
    }

    static Transform createTransform(int size) {
        if (size == 2)
            return Transform.IDENTITY;
        else {
            final TransformTranslate tr1 = new TransformTranslate(-70, -139);
            final TransformTranslate tr2 = new TransformTranslate(70, 139);
            float s = (2 + size) / 4f;
            final TransformMatrix trm = new TransformMatrix(s, 0, 0, s, 0, 0);
            return Transform.mul(tr1, Transform.mul(trm, tr2));
        }
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
