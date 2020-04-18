/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.switching.SwitchDT;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The switch shape
 */
public class SwitchDTShape implements Shape {

    private final PinDescriptions outputs;
    private final String label;
    private final int poles;
    private boolean closed;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public SwitchDTShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        closed = attributes.get(Keys.CLOSED);
        poles = attributes.get(Keys.POLES);
        label = attributes.getLabel();
    }

    @Override
    public Pins getPins() {
        Pins pins = new Pins();
        for (int p = 0; p < poles; p++) {
            pins
                    .add(new Pin(new Vector(0, SIZE * 2 * p), outputs.get(p * 3)))
                    .add(new Pin(new Vector(SIZE * 2, SIZE * 2 * p), outputs.get(p * 3 + 1)))
                    .add(new Pin(new Vector(SIZE * 2, SIZE + SIZE * 2 * p), outputs.get(p * 3 + 2)));
        }
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return new Interactor() {
            @Override
            public void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                closed = !closed;
                if (ioState != null)
                    modelSync.modify(() -> ((SwitchDT) element).setClosed(closed));
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int yOffs = 0;

        for (int p = 0; p < poles; p++)
            graphic.drawPolygon(new Polygon(false)
                    .add(SIZE * 2, p * SIZE * 2 + SIZE)
                    .add(SIZE * 2 - SIZE2 / 2, p * SIZE * 2 + SIZE)
                    .add(SIZE * 2 - SIZE2 / 2, p * SIZE * 2 + SIZE2 + 2), Style.NORMAL);

        if (closed) {
            for (int p = 0; p < poles; p++)
                graphic.drawLine(
                        new Vector(0, 2 * SIZE * p),
                        new Vector(SIZE * 2, 2 * SIZE * p), Style.NORMAL);
        } else {
            yOffs = -SIZE2 / 2;
            for (int p = 0; p < poles; p++)
                graphic.drawLine(
                        new Vector(0, 2 * SIZE * p),
                        new Vector(SIZE * 2 - 4, 2 * SIZE * p - yOffs * 2), Style.NORMAL);
        }
        graphic.drawLine(
                new Vector(SIZE, -yOffs + (poles - 1) * 2 * SIZE),
                new Vector(SIZE, -yOffs - SIZE), Style.DASH);
        graphic.drawLine(
                new Vector(SIZE2, -yOffs - SIZE),
                new Vector(SIZE + SIZE2, -yOffs - SIZE), Style.THIN);

        if (label != null && label.length() > 0)
            graphic.drawText(
                    new Vector(SIZE, 4 + (poles - 1) * 2 * SIZE + SIZE),
                    label, Orientation.CENTERTOP, Style.SHAPE_PIN);
    }

}
