/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.OutputShape.OUT_SIZE;

/**
 * The Clock shape
 */
public class ClockShape implements Shape {
    private static final int WI = OUT_SIZE / 3;
    private static final Vector POS = new Vector(-OUT_SIZE - WI * 2, WI);

    private final String label;
    private final PinDescriptions outputs;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ClockShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        String pinNumber = attr.get(Keys.PINNUMBER);
        if (pinNumber.length() == 0)
            this.label = attr.getLabel();
        else
            this.label = attr.getLabel() + " (" + pinNumber + ")";
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        ioState.getOutput(0).addObserverToValue(guiObserver); // necessary to replot wires also if component itself does not depend on state
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1) {
                    modelSync.access(() -> {
                        value.setValue(1 - value.getValue());
                    });
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(-OUT_SIZE * 2 - 1, -OUT_SIZE)
                .add(-1, -OUT_SIZE)
                .add(-1, OUT_SIZE)
                .add(-OUT_SIZE * 2 - 1, OUT_SIZE), Style.NORMAL);

        graphic.drawPolygon(new Polygon(false)
                .add(POS)
                .add(POS.add(WI, 0))
                .add(POS.add(WI, -WI * 2))
                .add(POS.add(2 * WI, -WI * 2))
                .add(POS.add(2 * WI, 0))
                .add(POS.add(3 * WI, 0))
                .add(POS.add(3 * WI, -WI * 2))
                .add(POS.add(4 * WI, -WI * 2)), Style.THIN);

        Vector textPos = new Vector(-OUT_SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
