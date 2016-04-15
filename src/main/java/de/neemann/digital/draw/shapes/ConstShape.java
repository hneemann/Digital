package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

/**
 * @author hneemann
 */
public class ConstShape implements Shape {

    private final PinDescription[] outputs;
    private String value;

    public ConstShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) {
        this.outputs = outputs;
        this.value = ObservableValue.getHexString(attr.get(AttributeKey.Value));
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs[0]));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        Vector textPos = new Vector(-3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), value, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
