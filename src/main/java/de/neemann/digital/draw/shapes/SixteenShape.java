/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.Value;
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

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * The shape to show a sixteen seg display.
 */
public class SixteenShape implements Shape {

    private static final Polygon[] POLYGONS = new Polygon[]{
            Polygon.createFromPath("m 10,5 L 29,5 33,10 28,14 9,14 5,10 z"), // 0,
            Polygon.createFromPath("m 38,5 L 57,5 62,10 57,14 38,14 33,10 z"), // 1,
            Polygon.createFromPath("m 53,65 L 57,14 62,10 66,14 63,65 57,70 z"), // 2,
            Polygon.createFromPath("m 49,126 L 52,75 57,70 62,75 58,126 53,130 z"), // 3,
            Polygon.createFromPath("m 30,126 L 49,126 53,130 48,135 29,135 25,130 z"), // 4,
            Polygon.createFromPath("m 1,126 L 20,126 25,130 20,135 1,135 -3,130 z"), // 5,
            Polygon.createFromPath("m -7,126 L -4,75 1,70 5,75 1,126 -3,130 z"), // 6,
            Polygon.createFromPath("m -3,65 L 0,14 5,10 9,14 6,65 1,70 z"), // 7,
            Polygon.createFromPath("m 6,65 L 25,65 29,70 24,75 5,75 1,70 z"), // 8,
            Polygon.createFromPath("m 34,65 L 53,65 57,70 52,75 33,75 29,70 z"), // 9,
            Polygon.createFromPath("m 14,14 L 26,56 25,65 20,65 8,24 9,14 z"), // 10,
            Polygon.createFromPath("m 25,65 L 28,14 33,10 38,14 34,65 29,70 z"), // 11,
            Polygon.createFromPath("m 52,14 L 35,56 34,65 39,65 56,24 57,14 z"), // 12,
            Polygon.createFromPath("m 44,126 L 32,84 33,75 38,75 50,116 49,126 z"), // 13,
            Polygon.createFromPath("m 20,126 L 24,75 29,70 33,75 30,126 25,130 z"), // 14,
            Polygon.createFromPath("m 6,126 L 23,84 24,75 19,75 2,116 1,126 z"), // 15,
    };
    private static final Vector DOT = new Vector(58, 127);


    private static final int HEIGHT = 7;

    private final Style onStyle;
    private final Style offStyle;
    private final PinDescriptions pins;
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
        offStyle = Style.NORMAL.deriveFillStyle(new Color(230, 230, 230));
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), pins.get(0)))
                .add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), pins.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        input = ioState.getInput(0).addObserverToValue(guiObserver);
        dp = ioState.getInput(1).addObserverToValue(guiObserver);
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
        graphic.drawPolygon(SevenShape.FRAME, Style.NORMAL);

        int bits = -1;
        if (inValue != null)
            bits = (int) inValue.getValue();

        int mask = 1;
        for (Polygon p : POLYGONS) {
            Style s = onStyle;
            if ((bits & mask) == 0) s = offStyle;
            graphic.drawPolygon(p, s);
            mask <<= 1;
        }

        Style s = onStyle;
        if (dpValue != null && !dpValue.getBool()) s = offStyle;
        graphic.drawCircle(DOT, DOT.add(8, 8), s);
    }

}
