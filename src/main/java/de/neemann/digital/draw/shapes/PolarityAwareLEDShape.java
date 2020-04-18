/*
 * Copyright (c) 2017 Helmut Neemann
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
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.PullDownShape.HEIGHT;
import static de.neemann.digital.draw.shapes.PullDownShape.WIDTH2;

/**
 * The polarity aware LED shape
 */
public class PolarityAwareLEDShape implements Shape {
    private static final int RAD = SIZE * 3 / 4;
    private final PinDescriptions inputs;
    private final Style style;
    private final String label;
    private ObservableValue aValue;
    private ObservableValue cValue;
    private Value a;
    private Value c;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public PolarityAwareLEDShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        style = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
        String l = attr.getLabel();
        if (l == null || l.trim().length() == 0)
            label = null;
        else
            label = l;
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, 0), inputs.get(0)))
                .add(new Pin(new Vector(0, SIZE * 4), inputs.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        aValue = ioState.getInput(0);
        cValue = ioState.getInput(1);
        return null;
    }

    @Override
    public void readObservableValues() {
        if (aValue != null && cValue != null) {
            a = aValue.getCopy();
            c = cValue.getCopy();
        }
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {

        graphic.drawPolygon(
                new Polygon(true)
                        .add(-WIDTH2, SIZE * 4 - SIZE2 - 1)
                        .add(-WIDTH2, SIZE * 4 - SIZE2 - HEIGHT)
                        .add(WIDTH2, SIZE * 4 - SIZE2 - HEIGHT)
                        .add(WIDTH2, SIZE * 4 - SIZE2 - 1),
                Style.NORMAL
        );
        graphic.drawLine(new Vector(0, SIZE * 4 - SIZE2), new Vector(0, SIZE * 4), Style.NORMAL);
        if (label != null) {
            Vector textPos = new Vector(SIZE + SIZE2, SIZE);
            graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.NORMAL);
        }

        if (a == null || c == null) {
            graphic.drawPolygon(
                    new Polygon(true)
                            .add(-SIZE2, 1 + SIZE2)
                            .add(SIZE2, 1 + SIZE2)
                            .add(0, -1 + SIZE + SIZE2),
                    Style.NORMAL
            );
            graphic.drawLine(new Vector(-SIZE2, -1 + SIZE + SIZE2), new Vector(SIZE2, -1 + SIZE + SIZE2), Style.NORMAL);
            graphic.drawLine(new Vector(0, -1 + SIZE + SIZE2), new Vector(0, SIZE * 4 - HEIGHT - SIZE2), Style.NORMAL);
            graphic.drawLine(new Vector(0, 0), new Vector(0, -1 + SIZE2), Style.NORMAL);

            graphic.drawLine(new Vector(SIZE - 0, SIZE2 + 2), new Vector(SIZE2 + 1, SIZE + 1), Style.THIN);
            graphic.drawLine(new Vector(SIZE - 2, SIZE2 + 1), new Vector(SIZE + 1, SIZE2 + 1), Style.THIN);
            graphic.drawLine(new Vector(SIZE + 1, SIZE2 + 4), new Vector(SIZE + 1, SIZE2 + 1), Style.THIN);
            graphic.drawLine(new Vector(SIZE + 6, SIZE2 + 8), new Vector(SIZE2 + 7, SIZE + 7), Style.THIN);
            graphic.drawLine(new Vector(SIZE + 4, SIZE2 + 7), new Vector(SIZE + 7, SIZE2 + 7), Style.THIN);
            graphic.drawLine(new Vector(SIZE + 7, SIZE2 + 10), new Vector(SIZE + 7, SIZE2 + 7), Style.THIN);
        } else {
            Vector center = new Vector(0, SIZE);
            Vector rad = new Vector(RAD, RAD);

            graphic.drawLine(new Vector(0, SIZE * 4 - SIZE2 - HEIGHT), new Vector(0, 0), Style.NORMAL);
            graphic.drawCircle(center.sub(rad), center.add(rad), Style.FILLED);

            boolean aActive = a.getBool() && !a.isHighZ();
            boolean cActive = !c.getBool() && !c.isHighZ();

            if (aActive && cActive) {
                Vector radL = new Vector(RAD - 2, RAD - 2);
                graphic.drawCircle(center.sub(radL), center.add(radL), style);
            }
        }
    }
}
