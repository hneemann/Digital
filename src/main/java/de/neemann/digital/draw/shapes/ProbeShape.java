/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

/**
 * The probe shape
 */
public class ProbeShape implements Shape {

    private final String label;
    private final PinDescriptions inputs;
    private final IntFormat format;
    private final boolean isLabel;
    private ObservableValue inValue;
    private Value inValueCopy;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ProbeShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        label = attr.getLabel();
        isLabel = label != null && label.length() > 0;
        this.format = attr.get(Keys.INT_FORMAT);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), inputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        inValue = ioState.getInput(0);
        return null;
    }

    @Override
    public void readObservableValues() {
        if (inValue != null)
            inValueCopy = inValue.getCopy();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int dy = -1;
        Orientation orientation = Orientation.LEFTCENTER;
        if (isLabel) {
            graphic.drawText(new Vector(2, -4), label, Orientation.LEFTBOTTOM, Style.NORMAL);
            dy = 4;
            orientation = Orientation.LEFTTOP;
        }
        String v = "?";
        if (inValueCopy != null)
            v = format.formatToView(inValueCopy);
        graphic.drawText(new Vector(2, dy), v, orientation, Style.NORMAL);

    }
}
