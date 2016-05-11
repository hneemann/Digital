package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.Interactor;
import de.neemann.digital.draw.shapes.Shape;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 Not Shape
 *
 * @author hneemann
 */
public class IEEENotShape implements Shape {
    private final PinDescription[] inputs;
    private final PinDescription[] outputs;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public IEEENotShape(PinDescription[] inputs, PinDescription[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(0, 0), inputs[0]));
            pins.add(new Pin(new Vector(SIZE * 2, 0), outputs[0]));
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
                        .add(1, -SIZE2 - 2)
                        .add(SIZE - 1, 0)
                        .add(1, SIZE2 + 2), Style.NORMAL
        );
        graphic.drawCircle(new Vector(SIZE + 1, -SIZE2 + 1),
                new Vector(SIZE * 2 - 1, SIZE2 - 1), Style.NORMAL);
    }
}
