package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.IOState;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;

/**
 * @author hneemann
 */
public class OutputShape implements Shape {
    public static final int SIZE = 8;
    public static final Vector RAD = new Vector(SIZE - 3, SIZE - 3);
    public static final Vector RADL = new Vector(SIZE, SIZE);
    private final String label;
    private IOState ioState;

    public OutputShape(String label) {
        this.label = label;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "in", Pin.Direction.input));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        ioState.getInput(0).addObserver(guiObserver);
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        Style style = Style.NORMAL;
        if (ioState != null) {
            ObservableValue value = ioState.getInput(0);
            long val = value.getValue();
            if (value.getBits() == 1) {
                if (val != 0)
                    style = Style.WIRE_HIGH;
                else
                    style = Style.WIRE_LOW;
            } else {
                Vector textPos = new Vector(2 + SIZE, -2 - SIZE);
                graphic.drawText(textPos, textPos.add(1, 0), Long.toHexString(val), Orientation.CENTERBOTTOM, Style.NORMAL);
            }
        }

        Vector center = new Vector(2 + SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawCircle(center.sub(RADL), center.add(RADL), Style.NORMAL);
        Vector textPos = new Vector(SIZE * 2 + 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.LEFTCENTER, Style.NORMAL);
    }
}
