package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
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
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
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
    public SplitterShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) throws BitsException {
        this.inputs = inputs;
        this.outputs = outputs;
        length = (Math.max(inputs.size(), outputs.size()) - 1) * SIZE + 2;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            for (int i = 0; i < inputs.size(); i++)
                pins.add(new Pin(new Vector(0, i * SIZE), inputs.get(i)));
            for (int i = 0; i < outputs.size(); i++)
                pins.add(new Pin(new Vector(SIZE, i * SIZE), outputs.get(i)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        for (int i = 0; i < inputs.size(); i++) {
            Vector pos = new Vector(-2, i * SIZE - 3);
            graphic.drawText(pos, pos.add(2, 0), inputs.get(i).getName(), Orientation.RIGHTBOTTOM, Style.SHAPE_SPLITTER);
            graphic.drawLine(new Vector(0, i * SIZE), new Vector(SIZE2, i * SIZE), Style.NORMAL);
        }
        for (int i = 0; i < outputs.size(); i++) {
            Vector pos = new Vector(SIZE + 2, i * SIZE - 3);
            graphic.drawText(pos, pos.add(2, 0), outputs.get(i).getName(), Orientation.LEFTBOTTOM, Style.SHAPE_SPLITTER);
            graphic.drawLine(new Vector(SIZE, i * SIZE), new Vector(SIZE2, i * SIZE), Style.NORMAL);
        }

        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2 - 2, -2)
                .add(SIZE2 + 2, -2)
                .add(SIZE2 + 2, length)
                .add(SIZE2 - 2, length), Style.FILLED);
    }
}
