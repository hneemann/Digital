/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import java.awt.*;

/**
 * The Break shape
 */
public class BreakShape implements Shape {
    private static final int SIZE = GenericShape.SIZE * 3 / 4;
    private static final int SIZEQ = SIZE / 2;
    private static final Vector RAD = new Vector(SIZE, SIZE);
    private static final Vector D1 = new Vector(SIZEQ, -SIZEQ);
    private static final Vector D2 = new Vector(SIZEQ, SIZEQ);
    private final String label;
    private final PinDescriptions inputs;
    private final boolean enabled;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public BreakShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.label = attr.getLabel();
        this.enabled = attr.get(Keys.ENABLED);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), inputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        Vector center = new Vector(2 + SIZE, 0);
        Style style = Style.NORMAL;
        if (!enabled)
            style = Style.DISABLED;
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawLine(center.sub(D1), center.add(D1), style);
        graphic.drawLine(center.sub(D2), center.add(D2), style);
        Vector textPos = new Vector(SIZE * 3, 0);
        graphic.drawText(textPos, label, Orientation.LEFTCENTER, style);
    }
}
