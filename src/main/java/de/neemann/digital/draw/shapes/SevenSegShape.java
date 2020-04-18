/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.CommonConnectionType;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * A seven seg display with seven single controllable inputs
 */
public class SevenSegShape extends SevenShape {
    private final PinDescriptions inputPins;
    private final boolean commonConnection;
    private final boolean persistence;
    private final boolean[] data;
    private final boolean anode;
    private ObservableValues inputValues;
    private Value[] inputs = new Value[8];
    private Value ccin;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public SevenSegShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        super(attr);
        this.inputPins = inputs;
        commonConnection = attr.get(Keys.COMMON_CONNECTION);
        anode = attr.get(Keys.COMMON_CONNECTION_TYPE).equals(CommonConnectionType.anode);
        persistence = attr.get(Keys.LED_PERSISTENCE);
        data = new boolean[8];
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(0, 0), inputPins.get(0)));
            pins.add(new Pin(new Vector(SIZE, 0), inputPins.get(1)));
            pins.add(new Pin(new Vector(SIZE * 2, 0), inputPins.get(2)));
            pins.add(new Pin(new Vector(SIZE * 3, 0), inputPins.get(3)));
            pins.add(new Pin(new Vector(0, SIZE * HEIGHT), inputPins.get(4)));
            pins.add(new Pin(new Vector(SIZE, SIZE * HEIGHT), inputPins.get(5)));
            pins.add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), inputPins.get(6)));
            pins.add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), inputPins.get(7)));
            if (commonConnection)
                pins.add(new Pin(new Vector(SIZE * 4, SIZE * HEIGHT), inputPins.get(8)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        inputValues = ioState.getInputs();
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        super.drawTo(graphic, highLight);
        if (commonConnection)
            graphic.drawLine(
                    new Vector(SIZE * 4 - SIZE2, SIZE * HEIGHT - 1),
                    new Vector(SIZE * 4, SIZE * HEIGHT - 1), Style.NORMAL);
    }

    @Override
    public void readObservableValues() {
        if (inputValues != null) {
            for (int i = 0; i < 8; i++)
                inputs[i] = inputValues.get(i).getCopy();
            if (commonConnection)
                ccin = inputValues.get(8).getCopy();
        }
    }

    @Override
    protected boolean getStyle(int i) {
        if (inputValues == null)
            return true;

        if (commonConnection) {
            boolean isHighZ = inputs[i].isHighZ() || ccin.isHighZ();
            boolean on = (inputs[i].getBool() != ccin.getBool()) && (inputs[i].getBool() ^ anode);
            if (persistence) {
                if (!isHighZ)
                    data[i] = on;
                return data[i];
            } else
                return !isHighZ && on;
        } else
            return !inputs[i].isHighZ() && inputs[i].getBool();

    }

}
