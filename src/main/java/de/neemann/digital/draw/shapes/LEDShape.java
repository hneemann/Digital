package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.core.element.PinInfo.input;
import static de.neemann.digital.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class LEDShape implements Shape {
    private static final Vector RAD = new Vector(SIZE - 2, SIZE - 2);
    private static final Vector RADL = new Vector(SIZE, SIZE);
    private final String label;
    private Style onStyle;
    private IOState ioState;

    public LEDShape(ElementAttributes attr) {
        this.label = attr.getLabel();
        onStyle = new Style(1, true, attr.get(AttributeKey.Color));
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), input("in")));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getInput(0).addObserverToValue(guiObserver);
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        boolean fill = true;
        if (ioState != null) {
            fill = false;
            ObservableValue value = ioState.getInput(0);
            if (!value.isHighZ() && (value.getValue() != 0))
                fill = true;
        }

        Vector center = new Vector(1 + SIZE, 0);
        graphic.drawCircle(center.sub(RADL), center.add(RADL), Style.FILLED);
        if (fill)
            graphic.drawCircle(center.sub(RAD), center.add(RAD), onStyle);
        Vector textPos = new Vector(SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.LEFTCENTER, Style.NORMAL);
    }
}
