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
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The output shape
 */
public class OutputShape implements Shape {
    /**
     * The size of the used grid
     */
    public static final int OUT_SIZE = GenericShape.SIZE * 3 / 4;
    /**
     * Inner circle size used for inputs and outputs
     */
    public static final Vector RAD = new Vector(OUT_SIZE - 6, OUT_SIZE - 6);


    static final Vector LATEX_RAD = new Vector(Style.MAXLINETHICK, Style.MAXLINETHICK);
    /**
     * Outer circle size used for inputs and outputs
     */
    public static final Vector RADL = new Vector(OUT_SIZE, OUT_SIZE);
    private final String label;
    private final PinDescriptions inputs;
    private final IntFormat format;
    private IOState ioState;
    private Value value;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public OutputShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        String pinNumber = attr.get(Keys.PINNUMBER);
        if (pinNumber.length() == 0)
            this.label = attr.getLabel();
        else
            this.label = attr.getLabel() + " (" + pinNumber + ")";

        format = attr.get(Keys.INT_FORMAT);
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
    public void drawTo(Graphic graphic, Style highLight) {
        if (graphic.isFlagSet(Graphic.Flag.smallIO)) {
            Vector center = new Vector(LATEX_RAD.x, 0);
            graphic.drawCircle(center.sub(LATEX_RAD), center.add(LATEX_RAD), Style.NORMAL);
            Vector textPos = new Vector(SIZE2 + LATEX_RAD.x, 0);
            graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.INOUT);
        } else {
            Style style = Style.NORMAL;
            if (value != null) {
                style = Style.getWireStyle(value);
                if (value.getBits() > 1) {
                    Vector textPos = new Vector(1 + OUT_SIZE, -4 - OUT_SIZE);
                    graphic.drawText(textPos, format.formatToView(value), Orientation.CENTERBOTTOM, Style.NORMAL);
                }
            }

            Vector center = new Vector(1 + OUT_SIZE, 0);
            graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
            graphic.drawCircle(center.sub(RADL), center.add(RADL), Style.NORMAL);
            Vector textPos = new Vector(OUT_SIZE * 3, 0);
            graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.INOUT);
        }
    }
}
