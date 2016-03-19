package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.parts.IOState;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;

import static de.neemann.digital.gui.draw.shapes.OutputShape.RAD;
import static de.neemann.digital.gui.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class InputShape implements Shape {

    private final int bits;
    private final String label;
    private IOState ioState;

    public InputShape(int bits, String label) {
        this.bits = bits;
        this.label = label;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "out", Pin.Direction.output));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getOutput(0).addObserver(guiObserver);
        return new Interactor() {
            @Override
            public void clicked(CircuitComponent cc, Vector pos, IOState ioState) {
                // toggle the output
                long v = ioState.getOutput(0).getValue();
                ioState.getOutput(0).setValue(1 - v);
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic) {
        Style style = Style.NORMAL;
        if (ioState != null) {
            if (ioState.getOutput(0).getValue() != 0)
                style = Style.WIRE_HIGH;
            else
                style = Style.WIRE_LOW;
        }

        Vector center = new Vector(-2 - SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
