package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
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
     */
    public TextShape(ElementAttributes attr) {
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
        int size = Style.NORMAL.getFontSize();
        graphic.drawPolygon(
                new Polygon(true)
                        .add(0, 0)
                        .add(size * 2, 0)
                        .add(size * 2, size)
                        .add(0, size), Style.INVISIBLE);
        graphic.drawText(new Vector(0, 0), new Vector(1, 0), label, Orientation.LEFTTOP, Style.NORMAL);
    }
}
