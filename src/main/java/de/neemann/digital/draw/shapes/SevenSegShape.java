package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * A seven seg display with seven single controllable inputs
 *
 * @author hneemann
 */
public class SevenSegShape extends SevenShape {
    private final PinDescriptions inputPins;
    private ObservableValues inputs;
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
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        inputs = ioState.getInputs();
        for (ObservableValue o : inputs)
            o.addObserverToValue(guiObserver);
        return null;
    }


    @Override
    protected boolean getStyle(int i) {
        if (inputs == null)
            return true;
        else return inputs.get(i).getValueIgnoreBurn() > 0;
    }

}
