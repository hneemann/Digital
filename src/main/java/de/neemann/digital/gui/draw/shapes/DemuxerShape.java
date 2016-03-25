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
public class DemuxerShape implements Shape {
    private final int outputCount;
    private Pins pins;

    public DemuxerShape(int selectorBits) {
        this.outputCount = 1 << selectorBits;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(SIZE, outputCount * SIZE), "sel", Pin.Direction.input));
            if (outputCount == 2) {
                pins.add(new Pin(new Vector(SIZE * 2, 0 * SIZE), "out_0", Pin.Direction.output));
                pins.add(new Pin(new Vector(SIZE * 2, 2 * SIZE), "out_1", Pin.Direction.output));
            } else
                for (int i = 0; i < outputCount; i++) {
                    pins.add(new Pin(new Vector(SIZE * 2, i * SIZE), "out_" + i, Pin.Direction.output));
                }
            pins.add(new Pin(new Vector(0, (outputCount / 2) * SIZE), "in", Pin.Direction.input));
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
                .add(2, 3)
                .add(SIZE * 2 - 2, -2)
                .add(SIZE * 2 - 2, outputCount * SIZE + 2)
                .add(2, outputCount * SIZE - 3), Style.NORMAL);
    }
}
