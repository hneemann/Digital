package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
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
    private final PinDescription[] inputPins;
    private ObservableValue[] inputs;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public SevenSegShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) {
        super(attr);
        this.inputPins = inputs;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(0, 0), inputPins[0]));
            pins.add(new Pin(new Vector(SIZE, 0), inputPins[1]));
            pins.add(new Pin(new Vector(SIZE * 2, 0), inputPins[2]));
            pins.add(new Pin(new Vector(SIZE * 3, 0), inputPins[3]));
            pins.add(new Pin(new Vector(0, SIZE * HEIGHT), inputPins[4]));
            pins.add(new Pin(new Vector(SIZE, SIZE * HEIGHT), inputPins[5]));
            pins.add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), inputPins[6]));
            pins.add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), inputPins[7]));
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
        else return inputs[i].getValueIgnoreBurn() > 0;
    }

}
