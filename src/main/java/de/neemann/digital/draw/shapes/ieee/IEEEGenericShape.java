/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.shapes.GenericShape;
import de.neemann.digital.draw.shapes.InteractorInterface;
import de.neemann.digital.draw.shapes.Shape;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 Shape
 */
public abstract class IEEEGenericShape implements Shape {

    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final boolean invert;
    private final InverterConfig inverterConfig;
    private final boolean wideShape;

    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if NAnd, NOr
     * @param attr    the elements attributes
     */
    public IEEEGenericShape(PinDescriptions inputs, PinDescriptions outputs, boolean invert, ElementAttributes attr) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.invert = invert;
        this.wideShape = attr.get(Keys.WIDE_SHAPE);
        inverterConfig = attr.get(Keys.INVERTER_CONFIG);
    }

    @Override
    public Pins getPins() {
        if (pins == null)
            pins = GenericShape.createPins(inputs, outputs, invert, inverterConfig, wideShape);
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int offs = (inputs.size() / 2 - 1) * SIZE;
        drawIEEE(new GraphicTransform(graphic, new TransformTranslate(new Vector(0, offs))));

        if (offs > 0) {
            graphic.drawLine(new Vector(1, 0), new Vector(1, offs - SIZE2 - 1), Style.NORMAL);
            int h = (inputs.size() / 2) * SIZE * 2;
            graphic.drawLine(new Vector(1, h), new Vector(1, h - offs + SIZE2 + 1), Style.NORMAL);
        }

        GenericShape.drawInputInvert(graphic, inverterConfig, getPins());

        if (invert) {
            int o = inputs.size() / 2 * SIZE;
            int pos = 3;
            if (wideShape)
                pos++;
            for (int i = 0; i < outputs.size(); i++)
                graphic.drawCircle(new Vector(SIZE * pos + 1, i * SIZE - SIZE2 + 1 + o),
                        new Vector(SIZE * (pos + 1) - 1, i * SIZE + SIZE2 - 1 + o), Style.NORMAL);
        }
    }

    /**
     * Draws the shape
     *
     * @param graphic the graphic instance to use
     */
    protected abstract void drawIEEE(Graphic graphic);

    /**
     * @return true is a wide shape is selected
     */
    public boolean isWideShape() {
        return wideShape;
    }
}
