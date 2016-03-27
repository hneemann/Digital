package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.OutputShape.SIZE;

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
        ioState.getOutput(0).addObserver(guiObserver); // necessary to replot wires also if component itself does not depend on state
        return new Interactor() {
            @Override
            public void clicked(CircuitComponent cc, Point pos, IOState ioState) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1) {
                    value.setValue(1 - value.getValue());
                }
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
