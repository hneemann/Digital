/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.switching.TransGate;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Shape of a transmission gate.
 */
public class TransGateShape implements Shape {
    private static final int RAD = 4;
    private static final int P = SIZE - 5;
    private static final Polygon TOP = new Polygon(true)
            .add(0, 0)
            .add(0, -SIZE)
            .add(SIZE * 2, 0)
            .add(SIZE * 2, -SIZE)
            .add(0, 0);
    private static final Polygon BOTTOM = new Polygon(true)
            .add(0, 0)
            .add(0, SIZE)
            .add(SIZE * 2, 0)
            .add(SIZE * 2, SIZE)
            .add(0, 0);

    private static final Transform TRANS_SWITCH = new TransformRotate(new Vector(SIZE2, SIZE + SIZE2), 1);

    private final PinDescriptions input;
    private final PinDescriptions output;
    private TransGate transGate;
    private boolean isClosed;

    /**
     * Creates a trantmission gate
     *
     * @param attr   the attrobutes
     * @param input  inputs
     * @param output outputs
     */
    public TransGateShape(ElementAttributes attr, PinDescriptions input, PinDescriptions output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(SIZE, -SIZE), input.get(0)))
                .add(new Pin(new Vector(SIZE, SIZE), input.get(1)))
                .add(new Pin(new Vector(0, 0), output.get(0)))
                .add(new Pin(new Vector(SIZE * 2, 0), output.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        transGate = (TransGate) ioState.getElement();
        return null;
    }

    @Override
    public void readObservableValues() {
        if (transGate != null)
            isClosed = transGate.isClosed();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawPolygon(TOP, Style.NORMAL);
        graphic.drawPolygon(BOTTOM, Style.NORMAL);
        graphic.drawLine(new Vector(SIZE, -SIZE), new Vector(SIZE, -SIZE2), Style.NORMAL);
        graphic.drawCircle(new Vector(SIZE - RAD, P - RAD), new Vector(SIZE + RAD, P + RAD), Style.NORMAL);

        if (transGate != null)
            FETShape.drawSwitch(new GraphicTransform(graphic, TRANS_SWITCH), isClosed);
    }

}
