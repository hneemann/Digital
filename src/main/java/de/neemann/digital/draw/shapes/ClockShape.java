/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.OutputShape.LATEX_RAD;
import static de.neemann.digital.draw.shapes.OutputShape.OUT_SIZE;

/**
 * The Clock shape
 */
public class ClockShape implements Shape {
    private final String label;
    private final PinDescriptions outputs;
    private final boolean small;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ClockShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        String pinNumber = attr.get(Keys.PINNUMBER);
        if (pinNumber.length() == 0)
            this.label = attr.getLabel();
        else
            this.label = attr.getLabel() + " (" + pinNumber + ")";

        small = attr.get(Keys.IN_OUT_SMALL);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return new Interactor() {
            @Override
            public void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1)
                    modelSync.modify(() -> value.setValue(1 - value.getValue()));
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        Vector wavePos;
        int waveSize = OUT_SIZE / 3;
        if (graphic.isFlagSet(Graphic.Flag.smallIO)) {
            Vector center = new Vector(-LATEX_RAD.x, 0);
            graphic.drawCircle(center.sub(LATEX_RAD), center.add(LATEX_RAD), Style.NORMAL);
            Vector textPos = new Vector(-SIZE2 - LATEX_RAD.x, 0);
            graphic.drawText(textPos, label, Orientation.RIGHTCENTER, Style.INOUT);
            wavePos = center.sub(new Vector(2 * waveSize, LATEX_RAD.y + waveSize + 1));
        } else {
            int outSize = OutputShape.getOutSize(small);
            waveSize = outSize / 3;
            graphic.drawPolygon(new Polygon(true)
                    .add(-outSize * 2 - 1, -outSize)
                    .add(-1, -outSize)
                    .add(-1, outSize)
                    .add(-outSize * 2 - 1, outSize), Style.NORMAL);

            Vector textPos = new Vector(-outSize * 3, 0);
            graphic.drawText(textPos, label, Orientation.RIGHTCENTER, Style.INOUT);

            wavePos = new Vector(-outSize - waveSize * 2, waveSize);
        }
        graphic.drawPolygon(new Polygon(false)
                .add(wavePos)
                .add(wavePos.add(waveSize, 0))
                .add(wavePos.add(waveSize, -waveSize * 2))
                .add(wavePos.add(2 * waveSize, -waveSize * 2))
                .add(wavePos.add(2 * waveSize, 0))
                .add(wavePos.add(3 * waveSize, 0))
                .add(wavePos.add(3 * waveSize, -waveSize * 2))
                .add(wavePos.add(4 * waveSize, -waveSize * 2)), Style.THIN);
    }
}
