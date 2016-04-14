package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.core.element.PinInfo.input;
import static de.neemann.digital.core.element.PinInfo.output;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * @author hneemann
 */
public class DriverShape implements Shape {
    private final boolean bottom;
    private Pins pins;

    public DriverShape(ElementAttributes attr) {
        this.bottom = attr.get(AttributeKey.FlipSelPositon);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(-SIZE, 0), input("in")));
            pins.add(new Pin(new Vector(0, bottom ? SIZE : -SIZE), input("sel")));
            pins.add(new Pin(new Vector(SIZE, 0), output("out")));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawPolygon(
                new Polygon(true)
                        .add(-SIZE + 1, -SIZE2 - 2)
                        .add(SIZE - 1, 0)
                        .add(-SIZE + 1, SIZE2 + 2), Style.NORMAL
        );
        if (bottom)
            graphic.drawLine(new Vector(0, SIZE), new Vector(0, 7), Style.NORMAL);
        else
            graphic.drawLine(new Vector(0, -SIZE), new Vector(0, -7), Style.NORMAL);
    }
}
