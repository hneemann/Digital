/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.StepperMotorUnipolar;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The stepper motor shape
 */
public class StepperMotorShape implements Shape {

    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final String label;
    private StepperMotorUnipolar motor;
    private int pos;
    private boolean error;

    /**
     * Creates a new instance
     *
     * @param attr    the motors attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public StepperMotorShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        if (inputs.size() == 4)
            return new Pins()
                    .add(new Pin(new Vector(-SIZE * 2, -SIZE), inputs.get(0)))
                    .add(new Pin(new Vector(-SIZE * 2, 0), inputs.get(1)))
                    .add(new Pin(new Vector(-SIZE * 2, SIZE), inputs.get(2)))
                    .add(new Pin(new Vector(-SIZE * 2, SIZE * 2), inputs.get(3)))
                    .add(new Pin(new Vector(SIZE * 3, -SIZE), outputs.get(0)))
                    .add(new Pin(new Vector(SIZE * 3, SIZE * 3), outputs.get(1)));
        else
            return new Pins()
                    .add(new Pin(new Vector(-SIZE * 2, -SIZE), inputs.get(0)))
                    .add(new Pin(new Vector(-SIZE * 2, 0), inputs.get(1)))
                    .add(new Pin(new Vector(-SIZE * 2, SIZE), inputs.get(2)))
                    .add(new Pin(new Vector(-SIZE * 2, SIZE * 2), inputs.get(3)))
                    .add(new Pin(new Vector(-SIZE * 2, SIZE * 3), inputs.get(4)))
                    .add(new Pin(new Vector(SIZE * 3, -SIZE), outputs.get(0)))
                    .add(new Pin(new Vector(SIZE * 3, SIZE * 3), outputs.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        motor = (StepperMotorUnipolar) ioState.getElement();
        return null;
    }

    @Override
    public void readObservableValues() {
        if (motor != null) {
            pos = motor.getPos();
            error = motor.isError();
        }
    }


    private static final Style ERROR_STYLE = Style.NORMAL.deriveColor(ColorKey.ERROR);

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        Polygon polygon = new Polygon()
                .add(-SIZE * 2, -SIZE - SIZE2)
                .add(SIZE * 3, -SIZE - SIZE2)
                .add(SIZE * 3, SIZE * 3 + SIZE2)
                .add(-SIZE * 2, SIZE * 3 + SIZE2);


        graphic.drawPolygon(polygon, Style.NORMAL);


        Vector center = new Vector(SIZE2, SIZE);
        int radius = SIZE * 2;
        Vector rad = new Vector(radius, radius);
        graphic.drawCircle(center.sub(rad), center.add(rad), Style.THIN);

        double alpha = 2 * Math.PI * pos / StepperMotorUnipolar.STEPS;
        VectorFloat pointer = new VectorFloat((float) (Math.sin(alpha) * radius), (float) (-Math.cos(alpha) * radius));

        if (error)
            graphic.drawLine(center, center.add(pointer), ERROR_STYLE);
        else
            graphic.drawLine(center, center.add(pointer), Style.NORMAL);

        if (label != null && !label.isEmpty())
            graphic.drawText(new Vector(SIZE2, -SIZE * 2), label, Orientation.CENTERBOTTOM, Style.NORMAL);

    }
}
