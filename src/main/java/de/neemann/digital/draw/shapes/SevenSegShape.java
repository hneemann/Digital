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
    private final int persistenceTime;
    private final boolean anode;
    private LEDState[] ledStates;
    private final boolean[] displayStates;
    private Pins pins;
    private PersistenceHandler persistenceHandler;

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
        persistenceTime = attr.get(Keys.LED_PERSIST_TIME);
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
        for (int i = 0; i < 8; i++) {
            LEDState ledState = createLEDState(i, ioState.getInputs());
            if (persistenceTime == 0)
                ledStates[i] = ledState;
            else
                ledStates[i] = persistenceHandler.persist(ledState, persistenceTime);
        }
        return null;
    }

    @Override
    public void registerModel(ModelCreator modelCreator, Model model, ModelEntry element) {
        if (persistenceTime > 0)
            persistenceHandler = model.getOrCreateObserver(PersistenceHandler.class, PersistenceHandler::new);
    }

    private LEDState createLEDState(int i, ObservableValues inputs) {
        if (commonConnection) {
            return new CommonConnection(inputs.get(i), inputs.get(8), anode);
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

    private static final class CommonConnection implements LEDState {
        private final ObservableValue led;
        private final ObservableValue cc;
        private final boolean anode;

        private CommonConnection(ObservableValue led, ObservableValue cc, boolean anode) {
            this.led = led;
            this.cc = cc;
            this.anode = anode;
        }

        @Override
        public boolean getState() {
            boolean highZ = led.isHighZ() || cc.isHighZ();
            boolean on = (led.getBool() != cc.getBool()) && (led.getBool() ^ anode);
            return !highZ && on;
        }
    }

    private static final class PersistenceOfVision implements LEDState {
        private final LEDState parent;
        private final int persistenceTime;
        private int timeVisible;

        private PersistenceOfVision(LEDState parent, int persistenceTime) {
            this.parent = parent;
            this.persistenceTime = persistenceTime;
        }

        @Override
        public boolean getState() {
            return timeVisible > 0;
        }

        public void check() {
            if (parent.getState())
                timeVisible = persistenceTime;
            if (timeVisible > 0)
                timeVisible--;
        }
    }

    private static final class PersistenceHandler implements ModelStateObserverTyped {
        private final ArrayList<PersistenceOfVision> segments;

        private PersistenceHandler() {
            segments = new ArrayList<>();
        }

        @Override
        public void handleEvent(ModelEvent event) {
            if (event.getType() == ModelEventType.STEP || event.getType() == ModelEventType.CHECKBURN)
                for (PersistenceOfVision ag : segments)
                    ag.check();
        }

        @Override
        public ModelEventType[] getEvents() {
            return new ModelEventType[]{ModelEventType.STEP, ModelEventType.CHECKBURN};
        }

        public PersistenceOfVision persist(LEDState state, int persistenceTime) {
            PersistenceOfVision ag = new PersistenceOfVision(state, persistenceTime);
            segments.add(ag);
            return ag;
        }
    }
}
