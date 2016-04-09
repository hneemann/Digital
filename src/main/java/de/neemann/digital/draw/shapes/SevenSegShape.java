package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class SevenSegShape extends SevenShape {
    private ObservableValue[] inputs;
    private Pins pins;

    public SevenSegShape(ElementAttributes attr) {
        super(attr);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(0, 0), "a", Pin.Direction.input));
            pins.add(new Pin(new Vector(SIZE, 0), "b", Pin.Direction.input));
            pins.add(new Pin(new Vector(SIZE * 2, 0), "c", Pin.Direction.input));
            pins.add(new Pin(new Vector(SIZE * 3, 0), "d", Pin.Direction.input));
            pins.add(new Pin(new Vector(0, SIZE * HEIGHT), "e", Pin.Direction.input));
            pins.add(new Pin(new Vector(SIZE, SIZE * HEIGHT), "f", Pin.Direction.input));
            pins.add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), "g", Pin.Direction.input));
            pins.add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), "dp", Pin.Direction.input));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        inputs = ioState.getInputs();
        for (ObservableValue o : inputs)
            o.addObserver(guiObserver);
        return null;
    }


    protected Style getStyle(int i) {
        if (inputs == null)
            return onStyle;
        else if (inputs[i].getValueIgnoreBurn() > 0)
            return onStyle;
        else
            return offStyle;
    }

}
