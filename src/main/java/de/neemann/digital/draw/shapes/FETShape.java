/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.switching.NFET;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * FET shape.
 */
public abstract class FETShape implements Shape {
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final String label;
    private int xOffs = SIZE2 - 2;
    private NFET fet;
    private boolean isClosed;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    FETShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        label = attributes.getLabel();
    }

    /**
     * Sets the gap width
     *
     * @param xOffs the gap width
     */
    void setXOffs(int xOffs) {
        this.xOffs = xOffs;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        fet = (NFET) ioState.getElement();
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        final int g = SIZE2 / 2;
        graphic.drawPolygon(new Polygon(false)
                .add(SIZE, 0)
                .add(xOffs, 0)
                .add(xOffs, SIZE2 - g), Style.NORMAL);

        graphic.drawPolygon(new Polygon(false)
                .add(SIZE, SIZE * 2)
                .add(xOffs, SIZE * 2)
                .add(xOffs, SIZE * 2 - SIZE2 + g), Style.NORMAL);

        graphic.drawLine(new Vector(xOffs, SIZE2 + g), new Vector(xOffs, SIZE + SIZE2 - g), Style.NORMAL);

        graphic.drawLine(new Vector(1, 0), new Vector(1, SIZE * 2), Style.NORMAL);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE + SIZE2, SIZE * 2), label, Orientation.LEFTBOTTOM, Style.SHAPE_PIN);

        if (fet != null)
            drawSwitch(graphic);
    }

    @Override
    public void readObservableValues() {
        if (fet!=null)
            isClosed=fet.isClosed();
    }

    /**
     * Draws the small switch beside the fet
     *
     * @param graphic the instance to draw to
     */
    private void drawSwitch(Graphic graphic) {
        drawSwitch(graphic, isClosed);
    }

    /**
     * draws the switch
     *
     * @param graphic the graphics instance to draw to
     * @param closed  state of the switch
     */
    public static void drawSwitch(Graphic graphic, boolean closed) {
        if (closed) {
            graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE + SIZE2, SIZE), Style.SHAPE_PIN);
        } else {
            graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE + SIZE2, SIZE2 / 2), Style.SHAPE_PIN);
            graphic.drawPolygon(new Polygon(false)
                    .add(SIZE + SIZE2 / 2, SIZE2 / 2 + 2)
                    .add(SIZE + SIZE2, SIZE - SIZE2 / 2)
                    .add(SIZE + SIZE2, SIZE), Style.SHAPE_PIN);
        }
    }

    /**
     * @return the inputs (gate)
     */
    public PinDescriptions getInputs() {
        return inputs;
    }

    /**
     * @return the outputs (source and drain)
     */
    public PinDescriptions getOutputs() {
        return outputs;
    }
}
