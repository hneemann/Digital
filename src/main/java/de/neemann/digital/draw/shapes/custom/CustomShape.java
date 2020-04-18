/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.InteractorInterface;
import de.neemann.digital.draw.shapes.Shape;

/**
 * Represents a custom shape.
 */
public class CustomShape implements Shape {
    private final String label;
    private final CustomShapeDescription shapeDescription;
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param shapeDescription the description of the shape
     * @param label            the label
     * @param inputs           the inputs of the component
     * @param outputs          the inputs of the component
     * @throws PinException thrown if a pin is not found
     */
    public CustomShape(CustomShapeDescription shapeDescription, String label, PinDescriptions inputs, PinDescriptions outputs) throws PinException {
        this.label = label;
        this.shapeDescription = shapeDescription;
        this.inputs = inputs;
        this.outputs = outputs;

        initPins();
    }

    private void initPins() throws PinException {
        pins = new Pins();
        for (PinDescription p : outputs)
            pins.add(new Pin(shapeDescription.getPin(p.getName()).getPos(), p));
        for (PinDescription p : inputs)
            pins.add(new Pin(shapeDescription.getPin(p.getName()).getPos(), p));
    }

    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        for (Drawable d : shapeDescription)
            d.drawTo(graphic, highLight);

        CustomShapeDescription.TextHolder l = shapeDescription.getLabel();
        if (l != null && label != null && !label.isEmpty())
            l.drawText(graphic, label);

        for (Pin p : getPins()) {
            try {
                CustomShapeDescription.Pin cp = shapeDescription.getPin(p.getName());
                if (cp != null && cp.isShowLabel()) {
                    if (p.getDirection() == Pin.Direction.input) {
                        graphic.drawText(p.getPos().add(4, 0), p.getName(), Orientation.LEFTCENTER, Style.SHAPE_PIN);
                    } else
                        graphic.drawText(p.getPos().add(-4, 0), p.getName(), Orientation.RIGHTCENTER, Style.SHAPE_PIN);

                }
            } catch (PinException e) {
                // do nothing on an error
            }
        }
    }
}
