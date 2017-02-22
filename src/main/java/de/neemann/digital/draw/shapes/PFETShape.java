package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The FETS shape
 */
public class PFETShape implements Shape {

    private final PinDescriptions inputs;
    private final PinDescriptions outputs;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public PFETShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, 0), inputs.get(0)))
                .add(new Pin(new Vector(SIZE, 0), outputs.get(0)))
                .add(new Pin(new Vector(SIZE, SIZE * 2), outputs.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        final int x1 = SIZE2 - 2;
        final int g = SIZE2 / 2;
        graphic.drawPolygon(new Polygon(false)
                .add(SIZE, 0)
                .add(x1, 0)
                .add(x1, SIZE2 - g), Style.NORMAL);

        graphic.drawPolygon(new Polygon(false)
                .add(SIZE, SIZE * 2)
                .add(x1, SIZE * 2)
                .add(x1, SIZE * 2 - SIZE2 + g), Style.NORMAL);

        graphic.drawLine(new Vector(x1, SIZE), new Vector(SIZE2, SIZE), Style.THIN);

        graphic.drawPolygon(new Polygon(true)
                .add(SIZE, SIZE)
                .add(x1 + 4, SIZE - SIZE2 / 3)
                .add(x1 + 4, SIZE + SIZE2 / 3), Style.THIN_FILLED);

        graphic.drawLine(new Vector(x1, SIZE2 + g), new Vector(x1, SIZE + SIZE2 - g), Style.NORMAL);

        graphic.drawLine(new Vector(1, 0), new Vector(1, SIZE * 2), Style.NORMAL);
    }

}
