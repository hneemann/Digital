package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The Splitter shape
 *
 * @author hneemann
 */
public class SplitterShape implements Shape {
    private final PinDescription[] inputs;
    private final PinDescription[] outputs;
    private final int length;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     * @throws BitsException BitsException
     */
    public SplitterShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) throws BitsException {
        this.inputs = inputs;
        this.outputs = outputs;
        length = (Math.max(inputs.length, outputs.length) - 1) * SIZE + 2;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            for (int i = 0; i < inputs.length; i++)
                pins.add(new Pin(new Vector(0, i * SIZE), inputs[i]));
            for (int i = 0; i < outputs.length; i++)
                pins.add(new Pin(new Vector(SIZE, i * SIZE), outputs[i]));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        for (int i = 0; i < inputs.length; i++) {
            Vector pos = new Vector(-2, i * SIZE - 3);
            graphic.drawText(pos, pos.add(2, 0), inputs[i].getName(), Orientation.RIGHTBOTTOM, Style.SHAPE_PIN);
            graphic.drawLine(new Vector(0, i * SIZE), new Vector(SIZE2, i * SIZE), Style.WIRE);
        }
        for (int i = 0; i < outputs.length; i++) {
            Vector pos = new Vector(SIZE + 2, i * SIZE - 3);
            graphic.drawText(pos, pos.add(2, 0), outputs[i].getName(), Orientation.LEFTBOTTOM, Style.SHAPE_PIN);
            graphic.drawLine(new Vector(SIZE, i * SIZE), new Vector(SIZE2, i * SIZE), Style.WIRE);
        }

        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2 - 2, -2)
                .add(SIZE2 + 2, -2)
                .add(SIZE2 + 2, length)
                .add(SIZE2 - 2, length), Style.FILLED);
    }
}
