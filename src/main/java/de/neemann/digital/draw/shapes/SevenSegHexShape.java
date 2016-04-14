package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinInfo;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class SevenSegHexShape extends SevenShape {
    private static final int[] TABLE = new int[]{0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07, 0x7f, 0x6f, 0x77, 0x7c, 0x39, 0x5e, 0x79, 0x71};
    private Pins pins;
    private ObservableValue input;
    private ObservableValue dp;

    public SevenSegHexShape(ElementAttributes attr) {
        super(attr);
    }

    @Override
    protected Style getStyle(int i) {
        if (input == null)
            return onStyle;

        if (i == 7) {
            if (dp.getBool())
                return onStyle;
            else
                return offStyle;
        } else {
            int v = (int) input.getValueIgnoreBurn() & 0xf;
            v = TABLE[v];
            if ((v & (1 << i)) != 0)
                return onStyle;
            else
                return offStyle;
        }
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins()
                    .add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), PinInfo.input("d")))
                    .add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), PinInfo.input("dp")));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        input = ioState.getInput(0).addObserverToValue(guiObserver);
        dp = ioState.getInput(1).addObserverToValue(guiObserver);
        return null;
    }
}
