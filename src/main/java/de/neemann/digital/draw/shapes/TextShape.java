package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

/**
 * Simple text
 *
 * @author hneemann
 */
public class TextShape implements Shape {
    private final String label;

    /**
     * Create a new instance
     *
     * @param attr attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public TextShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        String text = attr.getLabel();
        if (text.length() == 0)
            text = Lang.get("elem_Text");
        this.label = text;

    }

    @Override
    public Pins getPins() {
        return new Pins();
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawText(new Vector(0, 0), new Vector(1, 0), label, Orientation.LEFTTOP, Style.NORMAL_TEXT);
    }
}
