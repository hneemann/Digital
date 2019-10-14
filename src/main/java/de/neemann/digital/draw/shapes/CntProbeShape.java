/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.core.io.CntProbe;

/**
 * The probe shape
 */
public class CntProbeShape implements Shape {

    private final String label;
    private final PinDescriptions inputs;
    private final boolean isLabel;
    private CntProbe cntProbe;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public CntProbeShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        label = attr.getLabel();
        isLabel = label != null && label.length() > 0;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), inputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {//        inValue = ioState.getInput(0);
        cntProbe = (CntProbe) ioState.getElement();
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int dy = -1;
        Orientation orientation = Orientation.LEFTCENTER;
        if (isLabel) {
            graphic.drawText(new Vector(2, -4), new Vector(3, -4), label, Orientation.LEFTBOTTOM, Style.NORMAL);
            dy = 4;
            orientation = Orientation.LEFTTOP;
        }
        String v = "#";
        if (cntProbe != null)
            v = Integer.toString(cntProbe.getCnt());
        graphic.drawText(new Vector(2, dy), new Vector(3, dy), v, orientation, Style.NORMAL);

    }
}
