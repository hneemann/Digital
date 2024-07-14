/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The Muxer shape
 */
public class MuxerShape implements Shape {
    private final boolean flip;
    private final int inputCount;
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private Pins pins;
    private ObservableValue selector;
    private Value selectorValue;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public MuxerShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        inputCount = inputs.size() - 1;
        this.flip = attr.get(Keys.FLIP_SEL_POSITON);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(SIZE, flip ? 0 : inputCount * SIZE), inputs.get(0)));
            if (inputs.size() == 3) {
                pins.add(new Pin(new Vector(0, 0 * SIZE), inputs.get(1)));
                pins.add(new Pin(new Vector(0, 2 * SIZE), inputs.get(2)));
            } else
                for (int i = 0; i < inputCount; i++) {
                    pins.add(new Pin(new Vector(0, i * SIZE), inputs.get(i + 1)));
                }
            pins.add(new Pin(new Vector(SIZE * 2, (inputCount / 2) * SIZE), outputs.get(0)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        selector = ioState.getInput(0);
        return null;
    }

    @Override
    public void readObservableValues() {
        if (selector != null)
            selectorValue = selector.getCopy();
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(1, -4)
                .add(SIZE * 2 - 1, 5)
                .add(SIZE * 2 - 1, inputCount * SIZE - 5)
                .add(1, inputCount * SIZE + 4), Style.NORMAL);
        graphic.drawText(new Vector(3, 2), "0", Orientation.LEFTTOP, Style.SHAPE_PIN);

        if (selectorValue != null) {
            int in = (int) selectorValue.getValue() + 1;
            Pins p = getPins();
            if (in < p.size()) {
                Vector pos = p.get(in).getPos();
                int s = SIZE2 / 2;
                graphic.drawCircle(pos.add(-s, -s), pos.add(s, s), Style.THIN);
            }
        }
    }
}
