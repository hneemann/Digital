package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.OutputShape.SIZE;

/**
 * The Clock shape
 * @author hneemann
 */
public class ClockShape implements Shape {
    private static final int WI = SIZE / 3;
    private static final Vector POS = new Vector(-SIZE - WI * 2, WI);

    private final String label;
    private final PinDescription[] outputs;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ClockShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) {
        this.outputs = outputs;
        this.label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs[0]));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        ioState.getOutput(0).addObserverToValue(guiObserver); // necessary to replot wires also if component itself does not depend on state
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1) {
                    value.setValue(1 - value.getValue());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(-SIZE * 2 - 1, -SIZE)
                .add(-1, -SIZE)
                .add(-1, SIZE)
                .add(-SIZE * 2 - 1, SIZE), Style.NORMAL);

        graphic.drawPolygon(new Polygon(false)
                .add(POS)
                .add(POS.add(WI, 0))
                .add(POS.add(WI, -WI * 2))
                .add(POS.add(2 * WI, -WI * 2))
                .add(POS.add(2 * WI, 0))
                .add(POS.add(3 * WI, 0))
                .add(POS.add(3 * WI, -WI * 2))
                .add(POS.add(4 * WI, -WI * 2)), Style.THIN);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
