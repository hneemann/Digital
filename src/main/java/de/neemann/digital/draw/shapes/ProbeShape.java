package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

/**
 * The probe shape
 *
 * @author hneemann
 */
public class ProbeShape implements Shape {

    private final String label;
    private final PinDescriptions inputs;
    private final IntFormat format;
    private int bits;
    private ObservableValue inValue;
    private Value inValueCopy;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ProbeShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        String label = attr.getLabel();
        if (label == null || label.length() == 0)
            label = Lang.get("name");
        this.label = label;
        this.format = attr.get(Keys.INT_FORMAT);
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
    public void readObservableValues() {
        if (bits > 1)
            inValueCopy = inValue.getCopy();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawText(new Vector(2, -1), new Vector(3, -1), label, Orientation.LEFTBOTTOM, Style.NORMAL);
        if (bits > 1) {
            String v = format.formatToView(inValueCopy);
            graphic.drawText(new Vector(2, 1), new Vector(3, 1), v, Orientation.LEFTTOP, Style.NORMAL);
        }
    }
}
