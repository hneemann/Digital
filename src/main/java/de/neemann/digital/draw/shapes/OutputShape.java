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
import de.neemann.digital.core.ValueFormatter;
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
     * Size of the normal sized inputs and outputs
     */
    public static final int OUT_SIZE = GenericShape.SIZE * 3 / 4;

    /**
     * The size of the inputs and outputs
     *
     * @param small true if small symbol is used
     * @return the size
     */
    public static int getOutSize(boolean small) {
        if (small)
            return SIZE2;
        else
            return OUT_SIZE;
    }

    /**
     * The size of the inputs and outputs
     *
     * @param small true if small symbol is used
     * @return the size
     */
    public static Style getOutStyle(boolean small) {
        if (small)
            return Style.THIN;
        else
            return Style.NORMAL;
    }

    /**
     * Inner circle size used for inputs and outputs
     *
     * @param small true if small symbol is used
     * @return the output circle radius as a vector
     */
    public static Vector getOutRad(boolean small) {
        int s = getOutSize(small);
        return new Vector(s - 6, s - 6);
    }

    static final Vector LATEX_RAD = new Vector(Style.MAXLINETHICK, Style.MAXLINETHICK);
    private final String label;
    private final PinDescriptions inputs;
    private final ValueFormatter formatter;
    private final boolean small;
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

        formatter = attr.getValueFormatter();
        small = attr.get(Keys.IN_OUT_SMALL);
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
            int outSize = getOutSize(small);
            Style style = getOutStyle(small);
            if (value != null) {
                style = Style.getWireStyle(value);
                if (value.getBits() > 1) {
                    Vector textPos = new Vector(1 + outSize, -4 - outSize);
                    graphic.drawText(textPos, formatter.formatToView(value), Orientation.CENTERBOTTOM, Style.NORMAL);
                }
            }

            Vector radl = new Vector(outSize, outSize);
            Vector rad = getOutRad(small);

            Vector center = new Vector(1 + outSize, 0);
            graphic.drawCircle(center.sub(rad), center.add(rad), style);
            graphic.drawCircle(center.sub(radl), center.add(radl), Style.NORMAL);
            Vector textPos = new Vector(outSize * 3, 0);
            graphic.drawText(textPos, label, Orientation.LEFTCENTER, Style.INOUT);
        }
    }
}
