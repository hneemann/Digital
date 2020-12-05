/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The shape used for the scope.
 */
public class ScopeShape implements Shape {

    private static final Style TRACE_STYLE = Style.THIN.deriveColor(ColorKey.WIRE_LOW);
    private static final int BORDER = SIZE / 3;
    private static final Polygon OUTER = new Polygon()
            .add(2, SIZE2).add(2, -SIZE * 2).add(SIZE * 4, -SIZE * 2).add(SIZE * 4, SIZE2);
    private static final Polygon INNER = new Polygon()
            .add(2 + BORDER, SIZE2 - BORDER)
            .add(2 + BORDER, -SIZE * 2 + BORDER)
            .add(SIZE * 3 - BORDER, -SIZE * 2 + BORDER)
            .add(SIZE * 3 - BORDER, SIZE2 - BORDER)
            .roundEdges(BORDER * 2);
    private static final Polygon TRACE = new Polygon(false)
            .add(3 + BORDER, -BORDER)
            .add(BORDER + SIZE, -BORDER)
            .add(BORDER + SIZE, -BORDER - SIZE)
            .add(BORDER + SIZE * 2, -BORDER - SIZE)
            .add(BORDER + SIZE * 2, -BORDER)
            .add(SIZE * 3 - BORDER - 1, -BORDER);

    private final PinDescription clock;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ScopeShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.clock = inputs.get(0);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), clock));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawPolygon(OUTER, Style.NORMAL);
        graphic.drawPolygon(TRACE, TRACE_STYLE);
        graphic.drawPolygon(INNER, Style.THIN);
    }

}
