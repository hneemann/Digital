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
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.SingleValueDialog;
import de.neemann.gui.Screen;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.draw.shapes.OutputShape.*;

/**
 * The input shape
 */
public class InputShape implements Shape {

    private static final int SLIDER_HEIGHT = (int) (300 * Screen.getInstance().getScaling());

    private final String label;
    private final PinDescriptions outputs;
    private final IntFormat format;
    private final boolean isHighZ;
    private final boolean avoidLow;
    private final long min;
    private final long max;
    private final int bits;
    private IOState ioState;
    private SingleValueDialog dialog;
    private Value value;
    private Value inValue;

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

        avoidLow = isHighZ && attr.get(Keys.AVOID_ACTIVE_LOW);

        bits = attr.getBits();
        if (format.isSigned()) {
            max = Bits.mask(bits) >> 1;
            min = -max - 1;
        } else {
            min = 0;
            max = Bits.mask(bits);
        }
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        this.ioState = ioState;
        return new InputInteractor();
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
        if (graphic.isFlagSet(Graphic.Flag.smallIO)) {
            Vector center = new Vector(-LATEX_RAD.x, 0);
            graphic.drawCircle(center.sub(LATEX_RAD), center.add(LATEX_RAD), Style.NORMAL);
            Vector textPos = new Vector(-SIZE2 - LATEX_RAD.x, 0);
            graphic.drawText(textPos, label, Orientation.RIGHTCENTER, Style.INOUT);
        } else {
            Style style = Style.NORMAL;
            final Polygon box = new Polygon(true).add(-OUT_SIZE * 2 - 1, -OUT_SIZE).add(-1, -OUT_SIZE).add(-1, OUT_SIZE).add(-OUT_SIZE * 2 - 1, OUT_SIZE);
            if (value != null) {
                style = Style.getWireStyle(value);
                if (value.getBits() > 1) {
                    Value v = value;
                    if (inValue != null)
                        v = inValue;
                    Vector textPos = new Vector(-1 - OUT_SIZE, -4 - OUT_SIZE);
                    graphic.drawText(textPos, format.formatToView(v), Orientation.CENTERBOTTOM, Style.NORMAL);
                } else {
                    if (inValue != null && !inValue.isEqual(value))
                        graphic.drawPolygon(box, Style.getWireStyle(inValue));
                }
            }

            graphic.drawPolygon(box, Style.NORMAL);

            Vector center = new Vector(-1 - OUT_SIZE, 0);
            graphic.drawCircle(center.sub(RAD), center.add(RAD), style);

            Vector textPos = new Vector(-OUT_SIZE * 3, 0);
            graphic.drawText(textPos, label, Orientation.RIGHTCENTER, Style.INOUT);
        }
    }

    private class InputInteractor extends Interactor {
        private boolean isDrag;
        private Point startPos;
        private long startValue;
        private long lastValueSet;

        @Override
        public void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
            ObservableValue value = ioState.getOutput(0);
            if (bits == 1) {
                modelSync.modify(() -> {
                    if (isHighZ) {
                        if (value.isHighZ()) {
                            if (avoidLow)
                                value.setValue(1);
                            else
                                value.setValue(0);
                        } else if (value.getValue() == 0) value.setValue(1);
                        else value.setToHighZ();
                    } else
                        value.setValue(1 - value.getValue());
                });
            } else {
                if (dialog == null || !dialog.isVisible()) {
                    Model model = ((In) element).getModel();
                    dialog = new SingleValueDialog(model.getWindowPosManager().getMainFrame(), pos, label, value, isHighZ, model)
                            .setSelectedFormat(format);
                    dialog.setVisible(true);
                } else
                    dialog.requestFocus();

            }
        }

        @Override
        public void pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
            isDrag = false;
        }

        @Override
        public void dragged(CircuitComponent cc, Point posOnScreen, Vector pos, Transform transform, IOState ioState, Element element, SyncAccess modelSync) {
            ObservableValue value = ioState.getOutput(0);
            if (bits > 1 && !value.isHighZ()) {
                if (!isDrag) {
                    isDrag = true;
                    startPos = posOnScreen;
                    startValue = value.getValue();
                    lastValueSet = startValue;
                } else {
                    int delta = startPos.y - posOnScreen.y;
                    long v = startValue + (delta * max) / SLIDER_HEIGHT;
                    long val = Math.max(min, Math.min(v, max));
                    if (val != lastValueSet) {
                        modelSync.modify(() -> value.setValue(val));
                        lastValueSet = val;
                    }
                }
            }
        }
    }
}
