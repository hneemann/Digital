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
            Polygon.createFromPath("m 9,5 L 27,5 32,12 27,18 8,18 4,12 z"), //A1
            Polygon.createFromPath("m 37,5 L 55,5 60,12 55,18 36,18 32,12 z"), // A2
            Polygon.createFromPath("m 53,64 L 55,18 60,12 64,18 62,64 57,70 z"), // B
            Polygon.createFromPath("m 50,122 L 52,77 57,70 61,77 59,122 54,128 z"),  // C
            Polygon.createFromPath("m 31,122 L 50,122 54,128 49,135 31,135 26,128 z"),  // D1
            Polygon.createFromPath("m 3,122 L 22,122 26,128 21,135 3,135 -1,128 z"),  // D2
            Polygon.createFromPath("m -5,122 L -3,77 1,70 5,77 3,122 -1,128 z"), // E
            Polygon.createFromPath("m -2,64 L 0,18 4,12 8,18 6,64 1,70 z"), // F
            Polygon.createFromPath("m 6,64 L 25,64 29,70 24,77 5,77 1,70 z"), // G1
            Polygon.createFromPath("m 34,64 L 53,64 57,70 52,77 33,77 29,70 z"), // G2
            Polygon.createFromPath("m 13,18 L 25,51 25,64 20,64 7,31 8,18 z"), // H
            Polygon.createFromPath("m 25,64 L 27,18 32,12 36,18 34,64 29,70 z"), // J
            Polygon.createFromPath("m 50,18 L 35,51 34,64 39,64 54,31 55,18 z"), // K
            Polygon.createFromPath("m 45,122 L 33,90 33,77 38,77 51,109 50,122 z"), // L
            Polygon.createFromPath("m 22,122 L 24,77 29,70 33,77 31,122 26,128 z"), // M
            Polygon.createFromPath("m 8,122 L 23,90 24,77 19,77 4,109 3,122 z"),  // N
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
