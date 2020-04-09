/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.switching.RelayDT;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The RelayDT shape
 */
public class RelayDTShape implements Shape {

    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final String label;
    private final int poles;
    private RelayDT relay;
    private boolean relayIsClosed = false;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public RelayDTShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        label = attributes.getLabel();
        poles = attributes.get(Keys.POLES);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins()
                    .add(new Pin(new Vector(0, -SIZE * 2), inputs.get(0)))
                    .add(new Pin(new Vector(SIZE * 2, -SIZE * 2), inputs.get(1)));

            for (int p = 0; p < poles; p++)
                pins
                        .add(new Pin(new Vector(0, p * SIZE * 2), outputs.get(p * 3)))
                        .add(new Pin(new Vector(SIZE * 2, p * SIZE * 2), outputs.get(p * 3 + 1)))
                        .add(new Pin(new Vector(SIZE * 2, p * SIZE * 2 + SIZE), outputs.get(p * 3 + 2)));

        }
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        relay = (RelayDT) ioState.getElement();
        return null;
    }

    @Override
    public void readObservableValues() {
        if (relay != null)
            relayIsClosed = relay.isClosed();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int yOffs = 0;
        if (!relayIsClosed)
            yOffs = SIZE2 / 2;

        for (int p = 0; p < poles; p++) {
            graphic.drawPolygon(new Polygon(false)
                    .add(SIZE * 2, p * SIZE * 2 + SIZE)
                    .add(SIZE * 2 - SIZE2 / 2, p * SIZE * 2 + SIZE)
                    .add(SIZE * 2 - SIZE2 / 2, p * SIZE * 2 + SIZE2 + 2), Style.NORMAL);

            if (relayIsClosed) {
                graphic.drawLine(new Vector(0, p * SIZE * 2), new Vector(SIZE * 2, p * SIZE * 2), Style.NORMAL);
            } else {
                graphic.drawLine(new Vector(0, p * SIZE * 2), new Vector(SIZE * 2 - 4, p * SIZE * 2 + yOffs * 2), Style.NORMAL);
            }
        }
        graphic.drawLine(new Vector(SIZE, (poles - 1) * SIZE * 2 + yOffs), new Vector(SIZE, 1 - SIZE), Style.DASH);


        // the coil
        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2, -SIZE)
                .add(SIZE2, -SIZE * 3)
                .add(SIZE + SIZE2, -SIZE * 3)
                .add(SIZE + SIZE2, -SIZE), Style.NORMAL);

        graphic.drawLine(new Vector(SIZE2, -SIZE - SIZE2), new Vector(SIZE + SIZE2, -SIZE * 2 - SIZE2), Style.THIN);

        graphic.drawLine(new Vector(SIZE2, -SIZE * 2), new Vector(0, -SIZE * 2), Style.NORMAL);
        graphic.drawLine(new Vector(SIZE + SIZE2, -SIZE * 2), new Vector(SIZE * 2, -SIZE * 2), Style.NORMAL);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE, -SIZE * 3 - 4), label, Orientation.CENTERBOTTOM, Style.SHAPE_PIN);
    }
}
