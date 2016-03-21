package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.*;

import static de.neemann.digital.gui.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class ClockShape implements Shape {

    private final String label;

    public ClockShape(String label) {
        this.label = label;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "C", Pin.Direction.output));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        ioState.getOutput(0).addObserver(guiObserver);
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
