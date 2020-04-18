/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.OutputShape.OUT_SIZE;

/**
 * The LED shape
 */
public class LEDShape implements Shape {
    private final String label;
    private final PinDescriptions inputs;
    private final int size;
    private Style onStyle;
    private IOState ioState;
    private Value value;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public LEDShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.label = attr.getLabel();
        final int s = attr.get(Keys.LED_SIZE);
        this.size = s > 0 ? s * OUT_SIZE : SIZE2;
        onStyle = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), inputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        this.ioState = ioState;
        return null;
    }

    @Override
    public void readObservableValues() {
        if (ioState != null)
            value = ioState.getInput(0).getCopy();
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        boolean fill = true;
        if (value != null) {
            fill = false;
            if (!value.isHighZ() && (value.getValue() != 0))
                fill = true;
        }

        Vector rad = new Vector(size - 2, size - 2);
        Vector radL = new Vector(size, size);

        Vector center = new Vector(1 + size, 0);
        graphic.drawCircle(center.sub(radL), center.add(radL), Style.FILLED);
        if (fill)
            graphic.drawCircle(center.sub(rad), center.add(rad), onStyle);
        Vector textPos = new Vector(2 * size + OUT_SIZE, 0);
        graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.NORMAL);
    }
}
