/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.*;
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
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.ModelEntry;

import java.util.ArrayList;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * A seven seg display with seven single controllable inputs
 */
public class SevenSegShape extends SevenShape {
    private final PinDescriptions inputPins;
    private final boolean commonConnection;
    private final boolean persistence;
    private final boolean anode;
    private LEDState[] ledStates;
    private final boolean[] displayStates;
    private Pins pins;
    private SegmentUpdater segmentUpdater;

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
        displayStates = new boolean[8];
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
        ledStates = new LEDState[8];
        for (int i = 0; i < 8; i++)
            ledStates[i] = createLEDState(i, ioState.getInputs());
        return null;
    }

    @Override
    public void registerModel(ModelCreator modelCreator, Model model, ModelEntry element) {
        if (commonConnection && persistence)
            segmentUpdater = model.getOrCreateObserver(SegmentUpdater.class, SegmentUpdater::new);
    }

    private LEDState createLEDState(int i, ObservableValues inputs) {
        if (commonConnection) {
            if (persistence) {
                return new CommonConnectionPersist(inputs.get(i), inputs.get(8), segmentUpdater);
            } else
                return new CommonConnection(inputs.get(i), inputs.get(8));
        } else {
            ObservableValue in = inputs.get(i);
            return () -> !in.isHighZ() && in.getBool();
        }
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
        if (ledStates != null)
            for (int i = 0; i < 8; i++)
                displayStates[i] = ledStates[i].getState();
    }

    @Override
    protected boolean getStyle(int i) {
        if (ledStates == null)
            return true;
        else
            return displayStates[i];
    }

    interface LEDState {
        boolean getState();
    }

    //CHECKSTYLE.OFF: FinalClass
    private class CommonConnection implements LEDState {
        private final ObservableValue led;
        private final ObservableValue cc;

        private CommonConnection(ObservableValue led, ObservableValue cc) {
            this.led = led;
            this.cc = cc;
        }

        protected boolean isOn() {
            return (led.getBool() != cc.getBool()) && (led.getBool() ^ anode);
        }

        protected boolean isHighZ() {
            return led.isHighZ() || cc.isHighZ();
        }

        @Override
        public boolean getState() {
            return !isHighZ() && isOn();
        }
    }
    //CHECKSTYLE.ON: FinalClass

    private final class CommonConnectionPersist extends CommonConnection {
        private boolean led;

        private CommonConnectionPersist(ObservableValue led, ObservableValue cc, SegmentUpdater segmentUpdater) {
            super(led, cc);
            segmentUpdater.add(this);
        }

        @Override
        public boolean getState() {
            return led;
        }

        public void updateState() {
            if (!isHighZ())
                led = isOn();
        }
    }

    private static final class SegmentUpdater implements ModelStateObserverTyped {
        private final ArrayList<CommonConnectionPersist> segments;

        private SegmentUpdater() {
            segments = new ArrayList<>();
        }

        @Override
        public void handleEvent(ModelEvent event) {
            if (event.getType() == ModelEventType.STEP)
                for (CommonConnectionPersist c : segments)
                    c.updateState();
        }

        @Override
        public ModelEventType[] getEvents() {
            return new ModelEventType[]{ModelEventType.STEP};
        }

        public void add(CommonConnectionPersist commonConnectionPersist) {
            segments.add(commonConnectionPersist);
        }
    }
}
