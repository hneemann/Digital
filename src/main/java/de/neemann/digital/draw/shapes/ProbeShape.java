package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class ProbeShape implements Shape {

    private final String label;
    private IOState ioState;
    private int bits;

    public ProbeShape(ElementAttributes attr) {
        this.label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), input("in")));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getInput(0).addObserverToValue(guiObserver);
        bits = ioState.getInput(0).getBits();
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawText(new Vector(2, -1), new Vector(3, -1), label, Orientation.LEFTBOTTOM, Style.NORMAL);
        if (bits > 1) {
            String v = ioState.getInput(0).getValueString();
            graphic.drawText(new Vector(2, 1), new Vector(3, 1), v, Orientation.LEFTTOP, Style.NORMAL);
        }
    }
}
