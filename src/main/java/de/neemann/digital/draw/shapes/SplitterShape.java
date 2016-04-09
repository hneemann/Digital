package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * @author hneemann
 */
public class SplitterShape implements Shape {
    private final String[] inputs;
    private final String[] outputs;
    private final int length;
    private Pins pins;

    public SplitterShape(ElementAttributes attr) throws BitsException {
        String inputDef = attr.get(AttributeKey.InputSplit);
        String outputDef = attr.get(AttributeKey.OutputSplit);
        inputs = new Splitter.Ports(inputDef).getNames();
        outputs = new Splitter.Ports(outputDef).getNames();
        length = (Math.max(inputs.length, outputs.length) - 1) * SIZE + 2;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            for (int i = 0; i < inputs.length; i++)
                pins.add(new Pin(new Vector(0, i * SIZE), inputs[i], Pin.Direction.input));
            for (int i = 0; i < outputs.length; i++)
                pins.add(new Pin(new Vector(SIZE, i * SIZE), outputs[i], Pin.Direction.output));
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
            Vector pos = new Vector(-2, i * SIZE - 2);
            graphic.drawText(pos, pos.add(2, 0), inputs[i], Orientation.RIGHTBOTTOM, Style.SHAPE_PIN);
            graphic.drawLine(new Vector(0, i * SIZE), new Vector(SIZE2, i * SIZE), Style.WIRE);
        }
        for (int i = 0; i < outputs.length; i++) {
            Vector pos = new Vector(SIZE + 2, i * SIZE - 2);
            graphic.drawText(pos, pos.add(2, 0), outputs[i], Orientation.LEFTBOTTOM, Style.SHAPE_PIN);
            graphic.drawLine(new Vector(SIZE, i * SIZE), new Vector(SIZE2, i * SIZE), Style.WIRE);
        }

        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2 - 2, -2)
                .add(SIZE2 + 2, -2)
                .add(SIZE2 + 2, length)
                .add(SIZE2 - 2, length), Style.FILLED);
    }
}
