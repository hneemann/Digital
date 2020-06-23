/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * The shape to show a sixteen seg display.
 */
public class SixteenShape implements Shape {

    private static final Polygon[] POLYGONS = new Polygon[]{
            Polygon.createFromPath("m 12.04,5.0 L 25.41,5.0 30.76,10.61 24.89,16.22 11.52,16.22 6.17,10.61 z"), // 0,
            Polygon.createFromPath("m 39.43,5.0 L 52.8,5.0 58.15,10.61 52.28,16.22 38.91,16.22 33.56,10.61 z"), // 1,
            Polygon.createFromPath("m 59.49,12.01 L 64.84,17.62 62.74,62.99 56.87,68.6 51.52,62.99 53.62,17.62 z"), // 2,
            Polygon.createFromPath("m 56.74,71.4 L 62.09,77.01 60.0,122.38 54.13,127.99 48.78,122.38 50.88,77.01 z"), // 3,
            Polygon.createFromPath("m 33.94,123.78 L 47.32,123.78 52.67,129.39 46.8,135.0 33.43,135.0 28.08,129.39 z"), // 4,
            Polygon.createFromPath("m 6.55,123.78 L 19.93,123.78 25.28,129.39 19.41,135.0 6.04,135.0 0.69,129.39 z"), // 5,
            Polygon.createFromPath("m 1.96,71.4 L 7.31,77.01 5.22,122.38 -0.64,127.99 -5.99,122.38 -3.9,77.01 z"), // 6,
            Polygon.createFromPath("m 4.7,12.01 L 10.05,17.62 7.96,62.99 2.09,68.6 -3.25,62.99 -1.15,17.62 z"), // 7,
            Polygon.createFromPath("m 9.29,64.39 L 22.67,64.39 28.02,70.0 22.15,75.61 8.78,75.61 3.43,70.0 z"), // 8,
            Polygon.createFromPath("m 36.69,64.39 L 50.06,64.39 55.41,70.0 49.54,75.61 36.17,75.61 30.82,70.0 z"), // 9,
            Polygon.createFromPath("m 12.01,18.2 L 17.06,18.2 22.73,50.48 22.18,62.41 17.13,62.41 11.46,30.13 z"), // 10,
            Polygon.createFromPath("m 32.09,12.01 L 37.44,17.62 35.35,62.99 29.48,68.6 24.13,62.99 26.23,17.62 z"), // 11,
            Polygon.createFromPath("m 46.56,18.2 L 51.61,18.2 51.06,30.13 42.41,62.41 37.36,62.41 37.91,50.48 z"), // 12,
            Polygon.createFromPath("m 36.66,77.59 L 41.7,77.59 47.38,109.87 46.83,121.8 41.78,121.8 36.11,89.52 z"), // 13,
            Polygon.createFromPath("m 29.35,71.4 L 34.7,77.01 32.61,122.38 26.74,127.99 21.39,122.38 23.49,77.01 z"), // 14,
            Polygon.createFromPath("m 16.43,77.59 L 21.48,77.59 20.93,89.52 12.27,121.8 7.22,121.8 7.78,109.87 z"), // 15,
    };
    private static final Vector DOT = new Vector(58, 127);


    private static final int HEIGHT = 7;

    private final Style onStyle;
    private final Style offStyle;
    private final PinDescriptions pins;
    private final int size;
    private ObservableValue input;
    private ObservableValue dp;
    private Value inValue;
    private Value dpValue;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the shapes inputs
     * @param outputs the shapes outputs
     */
    public SixteenShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        pins = inputs;
        onStyle = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
        offStyle = Style.NORMAL.deriveFillStyle(ColorKey.GRID);
        size = attr.get(Keys.SEVEN_SEG_SIZE);
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), pins.get(0)))
                .add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), pins.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        input = ioState.getInput(0);
        dp = ioState.getInput(1);
        return null;
    }

    @Override
    public void readObservableValues() {
        if (input != null) {
            inValue = input.getCopy();
            dpValue = dp.getCopy();
        }
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        Transform tr = SevenShape.createTransform(size);
        graphic.drawPolygon(SevenShape.FRAME.transform(tr), Style.NORMAL);

        int bits = -1;
        if (inValue != null)
            bits = (int) inValue.getValue();

        int mask = 1;
        for (Polygon p : POLYGONS) {
            Style s = onStyle;
            if ((bits & mask) == 0) s = offStyle;
            graphic.drawPolygon(p.transform(tr), s);
            mask <<= 1;
        }

        Style s = onStyle;
        if (dpValue != null && !dpValue.getBool()) s = offStyle;
        graphic.drawCircle(DOT.transform(tr), DOT.add(8, 8).transform(tr), s);
    }

}
