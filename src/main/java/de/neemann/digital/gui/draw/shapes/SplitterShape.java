package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.*;

import static de.neemann.digital.gui.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class SplitterShape implements Shape {

    private final String[] inputs;
    private final String[] outputs;
    private final int length;
    private Pins pins;

    public SplitterShape(String inputDef, String outputDef) throws BitsException {
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
    public void drawTo(Graphic graphic) {
        for (int i = 0; i < inputs.length; i++) {
            Vector pos = new Vector(-2, i * SIZE - 2);
            graphic.drawText(pos, pos.add(2, 0), inputs[i], Orientation.RIGHTBOTTOM, Style.SHAPE_PIN);
        }
        for (int i = 0; i < outputs.length; i++) {
            Vector pos = new Vector(SIZE + 2, i * SIZE - 2);
            graphic.drawText(pos, pos.add(2, 0), outputs[i], Orientation.LEFTBOTTOM, Style.SHAPE_PIN);
        }

        graphic.drawPolygon(new Polygon(true)
                .add(2, -2)
                .add(SIZE - 2, -2)
                .add(SIZE - 2, length)
                .add(2, length), Style.FILLED);
    }
}
