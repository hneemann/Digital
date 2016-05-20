package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.IntFormat;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

/**
 * The probe shape
 * @author hneemann
 */
public class ProbeShape implements Shape {

    private final String label;
    private final PinDescriptions inputs;
    private final IntFormat format;
    private int bits;
    private ObservableValue inValue;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ProbeShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.label = attr.getLabel();
        this.format = attr.get(Keys.INTFORMAT);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), inputs.get(0)));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        inValue = ioState.getInput(0);
        inValue.addObserverToValue(guiObserver);
        bits = inValue.getBits();
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawText(new Vector(2, -1), new Vector(3, -1), label, Orientation.LEFTBOTTOM, Style.NORMAL);
        if (bits > 1) {
            String v = format.format(inValue);
            graphic.drawText(new Vector(2, 1), new Vector(3, 1), v, Orientation.LEFTTOP, Style.NORMAL);
        }
    }
}
