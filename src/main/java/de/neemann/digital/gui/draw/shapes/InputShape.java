package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.SingleValueDialog;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.graphics.Polygon;

import java.awt.*;

import static de.neemann.digital.gui.draw.shapes.OutputShape.RAD;
import static de.neemann.digital.gui.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class InputShape implements Shape {

    private final String label;
    private IOState ioState;

    public InputShape(String label) {
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
            public void clicked(CircuitComponent cc, Point pos, IOState ioState) {
                ObservableValue value = ioState.getOutput(0);
                if (value.getBits() == 1) {
                    value.setValue(1 - value.getValue());
                } else {
                    SingleValueDialog.editValue(pos, value);
                }
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic) {
        Style style = Style.NORMAL;
        if (ioState != null) {
            ObservableValue value = ioState.getOutput(0);
            long val = value.getValue();
            if (value.getBits() == 1) {
                if (val != 0)
                    style = Style.WIRE_HIGH;
                else
                    style = Style.WIRE_LOW;
            } else {
                Vector textPos = new Vector(-2 - SIZE, -2 - SIZE);
                graphic.drawText(textPos, textPos.add(1, 0), Long.toHexString(val), Orientation.CENTERBOTTOM, Style.NORMAL);
            }
        }

        Vector center = new Vector(-2 - SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
