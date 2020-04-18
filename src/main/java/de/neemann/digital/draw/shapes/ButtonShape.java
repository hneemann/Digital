/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.Button;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.OutputShape.OUT_SIZE;

/**
 * The Button shape
 */
public class ButtonShape implements Shape {

    protected static final int HEIGHT = OUT_SIZE / 2;

    private final String label;
    private final PinDescriptions outputs;
    private Button button;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ButtonShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        this.label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        this.button = (Button) ioState.getElement();
        return new InteractorInterface() {
            @Override
            public void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
            }

            @Override
            public void pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                modelSync.modify(() -> button.setPressed(true));
            }

            @Override
            public void released(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                modelSync.modify(() -> button.setPressed(false));
            }

            @Override
            public void dragged(CircuitComponent cc, Point posOnScreen, Vector pos, Transform trans, IOState ioState, Element element, SyncAccess modelSync) {
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        boolean isPressed = false;
        if (button != null) isPressed = button.isPressed();

        if (isPressed) {
            graphic.drawPolygon(new Polygon(true)
                    .add(-OUT_SIZE * 2 - 1, -OUT_SIZE)
                    .add(-1, -OUT_SIZE)
                    .add(-1, OUT_SIZE)
                    .add(-OUT_SIZE * 2 - 1, OUT_SIZE), Style.NORMAL);
        } else {
            int t = Style.NORMAL.getThickness() / 4;
            graphic.drawPolygon(new Polygon(true)
                    .add(-OUT_SIZE * 2 - 1 - HEIGHT, -OUT_SIZE - HEIGHT)
                    .add(-1 - HEIGHT, -OUT_SIZE - HEIGHT)
                    .add(-1, -OUT_SIZE)
                    .add(-1, OUT_SIZE)
                    .add(-OUT_SIZE * 2 - 1, OUT_SIZE)
                    .add(-OUT_SIZE * 2 - 1 - HEIGHT, OUT_SIZE - HEIGHT), Style.NORMAL);
            graphic.drawPolygon(new Polygon(false)
                    .add(-1 - HEIGHT, -OUT_SIZE + t - HEIGHT)
                    .add(-1 - HEIGHT, OUT_SIZE - HEIGHT)
                    .add(t - OUT_SIZE * 2 - 1 - HEIGHT, OUT_SIZE - HEIGHT), Style.NORMAL);
            graphic.drawLine(new Vector(-1 - HEIGHT, OUT_SIZE - HEIGHT), new Vector(-1 - t, OUT_SIZE - t), Style.NORMAL);
        }

        Vector textPos = new Vector(-OUT_SIZE * 3, -4);
        graphic.drawText(textPos, label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
