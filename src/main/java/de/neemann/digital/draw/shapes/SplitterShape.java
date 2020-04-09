/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The Splitter shape
 */
public class SplitterShape implements Shape {
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final int length;
    private final int spreading;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     * @throws BitsException BitsException
     */
    public SplitterShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) throws BitsException {
        this.inputs = inputs;
        this.outputs = outputs;
        spreading = attr.get(Keys.SPLITTER_SPREADING);
        length = (Math.max(inputs.size(), outputs.size()) - 1) * spreading * SIZE + 2;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            for (int i = 0; i < inputs.size(); i++)
                pins.add(new Pin(new Vector(0, i * spreading * SIZE), inputs.get(i)));
            for (int i = 0; i < outputs.size(); i++)
                pins.add(new Pin(new Vector(SIZE, i * spreading * SIZE), outputs.get(i)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        for (int i = 0; i < inputs.size(); i++) {
            Vector pos = new Vector(-2, i * spreading * SIZE - 3);
            graphic.drawText(pos, inputs.get(i).getName(), Orientation.RIGHTBOTTOM, Style.SHAPE_SPLITTER);
            graphic.drawLine(new Vector(0, i * spreading * SIZE), new Vector(SIZE2, i * spreading * SIZE), Style.NORMAL);
        }
        for (int i = 0; i < outputs.size(); i++) {
            Vector pos = new Vector(SIZE + 2, i * spreading * SIZE - 3);
            graphic.drawText(pos, outputs.get(i).getName(), Orientation.LEFTBOTTOM, Style.SHAPE_SPLITTER);
            graphic.drawLine(new Vector(SIZE, i * spreading * SIZE), new Vector(SIZE2, i * spreading * SIZE), Style.NORMAL);
        }

        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2 - 2, -2)
                .add(SIZE2 + 2, -2)
                .add(SIZE2 + 2, length)
                .add(SIZE2 - 2, length), Style.FILLED);
    }
}
