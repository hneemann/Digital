/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.In;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.SingleValueDialog;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.OutputShape.*;

/**
 * The input shape
 */
public class InputShape implements Shape {

    private final String label;
    private final PinDescriptions outputs;
    private final IntFormat format;
    private IOState ioState;
    private SingleValueDialog dialog;
    private Value value;
    private Value inValue;
    private final boolean isHighZ;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public InputShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        String pinNumber = attr.get(Keys.PINNUMBER);
        if (pinNumber.length() == 0)
            this.label = attr.getLabel();
        else
            this.label = attr.getLabel() + " (" + pinNumber + ")";

        format = attr.get(Keys.INT_FORMAT);

        isHighZ = attr.get(Keys.INPUT_DEFAULT).isHighZ() || attr.get(Keys.IS_HIGH_Z);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getOutput(0).addObserverToValue(guiObserver);
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1) {
                    modelSync.access(() -> {
                        if (isHighZ) {
                            if (value.isHighZ()) value.setValue(0);
                            else if (value.getValue() == 0) value.setValue(1);
                            else value.setToHighZ();
                        } else
                            value.setValue(1 - value.getValue());
                    });
                    return true;
                } else {
                    if (dialog == null || !dialog.isVisible()) {
                        Model model = ((In) element).getModel();
                        dialog = new SingleValueDialog(model.getWindowPosManager().getMainFrame(), pos, label, value, isHighZ, cc, model);
                        dialog.setVisible(true);
                    } else
                        dialog.requestFocus();

                    return false;
                }
            }
        };
    }

    /**
     * @return the output connected to this shape
     */
    public ObservableValue getObservableValue() {
        if (ioState == null)
            return null;
        else
            return ioState.getOutput(0);
    }

    @Override
    public void readObservableValues() {
        if (ioState != null) {
            value = ioState.getOutput(0).getCopy();
            if (ioState.inputCount() == 1)
                inValue = ioState.getInput(0).getCopy();
        }
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        if (graphic.isFlagSet(Graphic.LATEX)) {
            Vector center = new Vector(-LATEX_RAD.x, 0);
            graphic.drawCircle(center.sub(LATEX_RAD), center.add(LATEX_RAD), Style.NORMAL);
            Vector textPos = new Vector(-SIZE2 - LATEX_RAD.x, 0);
            graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.INOUT);
        } else {
            Style style = Style.NORMAL;
            final Polygon box = new Polygon(true).add(-OUT_SIZE * 2 - 1, -OUT_SIZE).add(-1, -OUT_SIZE).add(-1, OUT_SIZE).add(-OUT_SIZE * 2 - 1, OUT_SIZE);
            if (value != null) {
                style = Style.getWireStyle(value);
                if (value.getBits() > 1) {
                    Vector textPos = new Vector(-1 - OUT_SIZE, -4 - OUT_SIZE);
                    graphic.drawText(textPos, textPos.add(1, 0), format.formatToView(value), Orientation.CENTERBOTTOM, Style.NORMAL);
                } else {
                    if (inValue != null && !inValue.isEqual(value))
                        graphic.drawPolygon(box, Style.getWireStyle(inValue));
                }
            }

            graphic.drawPolygon(box, Style.NORMAL);

            Vector center = new Vector(-1 - OUT_SIZE, 0);
            graphic.drawCircle(center.sub(RAD), center.add(RAD), style);

            Vector textPos = new Vector(-OUT_SIZE * 3, 0);
            graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.INOUT);
        }
    }
}
