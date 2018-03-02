/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The Vcc shape
 */
public class VDDShape implements Shape {
    static final int DOWNSHIFT = 4;
    private final PinDescriptions outputs;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public VDDShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        graphic.drawPolygon(
                new Polygon(false)
                        .add(-SIZE2, DOWNSHIFT)
                        .add(0, DOWNSHIFT - SIZE * 2 / 3)
                        .add(SIZE2, DOWNSHIFT), Style.NORMAL);
        graphic.drawLine(new Vector(0, -SIZE2 + DOWNSHIFT), new Vector(0, 0), Style.NORMAL);
    }
}
