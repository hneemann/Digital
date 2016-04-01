package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class DemuxerShape implements Shape {
    private final int outputCount;
    private final boolean hasInput;
    private final boolean flip;
    private final int height;
    private Pins pins;

    public DemuxerShape(int selectorBits, boolean hasInput, boolean flip) {
        this.hasInput = hasInput;
        this.flip = flip;
        outputCount = 1 << selectorBits;
        height = hasInput || (outputCount <= 2) ? outputCount * SIZE : (outputCount - 1) * SIZE;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(SIZE, flip ? 0 : height), "sel", Pin.Direction.input));
            if (outputCount == 2) {
                pins.add(new Pin(new Vector(SIZE * 2, 0 * SIZE), "out_0", Pin.Direction.output));
                pins.add(new Pin(new Vector(SIZE * 2, 2 * SIZE), "out_1", Pin.Direction.output));
            } else
                for (int i = 0; i < outputCount; i++) {
                    pins.add(new Pin(new Vector(SIZE * 2, i * SIZE), "out_" + i, Pin.Direction.output));
                }
            if (hasInput)
                pins.add(new Pin(new Vector(0, (outputCount / 2) * SIZE), "in", Pin.Direction.input));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(1, 5)
                .add(SIZE * 2 - 1, -4)
                .add(SIZE * 2 - 1, height + 4)
                .add(1, height - 5), Style.NORMAL);
        graphic.drawText(new Vector(SIZE * 2 - 3, 0), new Vector(SIZE * 2, 0), "0", Orientation.RIGHTTOP, Style.SHAPE_PIN);
    }
}
