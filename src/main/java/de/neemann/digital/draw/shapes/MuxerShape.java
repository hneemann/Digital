package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class MuxerShape implements Shape {
    private final boolean flip;
    private final int inputCount;
    private final PinDescription[] inputs;
    private final PinDescription[] outputs;
    private Pins pins;

    public MuxerShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        inputCount = inputs.length - 1;
        this.flip = attr.get(AttributeKey.FlipSelPositon);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(SIZE, flip ? 0 : inputCount * SIZE), inputs[0]));
            if (inputs.length == 3) {
                pins.add(new Pin(new Vector(0, 0 * SIZE), inputs[1]));
                pins.add(new Pin(new Vector(0, 2 * SIZE), inputs[2]));
            } else
                for (int i = 0; i < inputCount; i++) {
                    pins.add(new Pin(new Vector(0, i * SIZE), inputs[i + 1]));
                }
            pins.add(new Pin(new Vector(SIZE * 2, (inputCount / 2) * SIZE), outputs[0]));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(1, -4)
                .add(SIZE * 2 - 1, 5)
                .add(SIZE * 2 - 1, inputCount * SIZE - 5)
                .add(1, inputCount * SIZE + 4), Style.NORMAL);
        graphic.drawText(new Vector(3, 0), new Vector(4, 0), "0", Orientation.LEFTTOP, Style.SHAPE_PIN);
    }
}
