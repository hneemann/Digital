/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.ObservableValue;
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

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.OutputShape.OUT_SIZE;

/**
 * The LED shape
 */
public class RGBLEDShape implements Shape {
    private final String label;
    private final PinDescriptions inputs;
    private final long max;
    private final int size;
    private IOState ioState;
    private Color color;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public RGBLEDShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.label = attr.getLabel();
        max = Bits.mask(attr.getBits());
        final int s = attr.get(Keys.LED_SIZE);
        this.size = s > 0 ? s * OUT_SIZE : SIZE2;
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, -SIZE), inputs.get(0)))
                .add(new Pin(new Vector(0, 0), inputs.get(1)))
                .add(new Pin(new Vector(0, SIZE), inputs.get(2)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        this.ioState = ioState;
        return null;
    }

    @Override
    public void readObservableValues() {
        if (ioState != null) {
            long r = getCol(ioState.getInput(0));
            long g = getCol(ioState.getInput(1));
            long b = getCol(ioState.getInput(2));
            color = new Color((int) r, (int) g, (int) b);
        }
    }

    long getCol(ObservableValue c) {
        if (c.isHighZ())
            return 0;
        else
            return c.getValue() * 255 / max;
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        if (color == null)
            color = Color.RED;

        Vector rad = new Vector(size - 2, size - 2);
        Vector radL = new Vector(size, size);

        Vector center = new Vector(1 + size, 0);
        graphic.drawLine(new Vector(0, -SIZE), center, Style.NORMAL);
        graphic.drawLine(new Vector(0, SIZE), center, Style.NORMAL);

        graphic.drawCircle(center.sub(radL), center.add(radL), Style.FILLED);
        graphic.drawCircle(center.sub(rad), center.add(rad), Style.FILLED.deriveColor(color));
        Vector textPos = new Vector(2 * size + OUT_SIZE, 0);
        graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.NORMAL);
    }
}
