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
 * A pull up resistor shape.
 * Created by hneemann on 01.11.16.
 */
public class PullUpShape implements Shape {
    private static final int SIZE4 = SIZE / 4;

    private final PinDescriptions outputs;

    /**
     * Creates a new instance
     *
     * @param attributes attributes
     * @param inputs     inputs
     * @param outputs    outputs
     */
    public PullUpShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawPolygon(
                new Polygon(true)
                        .add(-SIZE4, -1)
                        .add(-SIZE4, -SIZE)
                        .add(SIZE4, -SIZE)
                        .add(SIZE4, -1),
                Style.NORMAL
        );
        int o = 4;
        graphic.drawLine(new Vector(0, -SIZE), new Vector(0, -SIZE - SIZE - o), Style.NORMAL);
        graphic.drawPolygon(
                new Polygon(false)
                        .add(-SIZE2, -SIZE - SIZE2 - o)
                        .add(0, -SIZE - o - SIZE2 - SIZE * 2 / 3)
                        .add(SIZE2, -SIZE - SIZE2 - o),
                Style.NORMAL
        );
    }

}
