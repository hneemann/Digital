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
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.graphics.Style.DISABLED;
import static de.neemann.digital.draw.graphics.Style.NORMAL;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The shape to visualize a test case
 */
public class GenericInitCodeShape implements Shape {

    private final String label;
    private final boolean enabled;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     inputs
     * @param outputs    outputs
     */
    public GenericInitCodeShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        label = attributes.getLabel();
        enabled = attributes.get(Keys.ENABLED);
    }

    @Override
    public Pins getPins() {
        return new Pins();
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        if (!graphic.isFlagSet(Graphic.Flag.hideTest)) {
            Polygon pol = new Polygon(true)
                    .add(SIZE2, SIZE2)
                    .add(SIZE2 + SIZE * 4, SIZE2)
                    .add(SIZE2 + SIZE * 4, SIZE * 2 + SIZE2)
                    .add(SIZE2, SIZE * 2 + SIZE2);
            Style textStyle = NORMAL;
            if (enabled) {
                graphic.drawPolygon(pol, NORMAL);
                graphic.drawPolygon(pol, Style.THIN);
            } else {
                graphic.drawPolygon(pol, DISABLED);
                textStyle = DISABLED;
            }

            graphic.drawText(new Vector(SIZE2 + SIZE * 2, SIZE + SIZE2), "init", Orientation.CENTERCENTER, textStyle);
            graphic.drawText(new Vector(SIZE2 + SIZE * 2, 0), label, Orientation.CENTERBOTTOM, textStyle);
        }
    }
}
