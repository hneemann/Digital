package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;

import static de.neemann.digital.gui.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class MuxerShape implements Shape {
    private final int inputCount;
    private final boolean flip;
    private Pins pins;

    public MuxerShape(int selectorBits, boolean flip) {
        this.flip = flip;
        this.inputCount = 1 << selectorBits;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(SIZE, flip ? 0 : inputCount * SIZE), "sel", Pin.Direction.input));
            if (inputCount == 2) {
                pins.add(new Pin(new Vector(0, 0 * SIZE), "in_0", Pin.Direction.input));
                pins.add(new Pin(new Vector(0, 2 * SIZE), "in_1", Pin.Direction.input));
            } else
                for (int i = 0; i < inputCount; i++) {
                    pins.add(new Pin(new Vector(0, i * SIZE), "in_" + i, Pin.Direction.input));
                }
            pins.add(new Pin(new Vector(SIZE * 2, (inputCount / 2) * SIZE), "out", Pin.Direction.output));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawPolygon(new Polygon(true)
                .add(2, -2)
                .add(SIZE * 2 - 2, 3)
                .add(SIZE * 2 - 2, inputCount * SIZE - 3)
                .add(2, inputCount * SIZE + 2), Style.NORMAL);
    }
}
