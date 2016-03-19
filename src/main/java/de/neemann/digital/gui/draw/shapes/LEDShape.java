package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.IOState;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;

import java.awt.*;

import static de.neemann.digital.gui.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class LEDShape implements Shape {
    public static final Vector RAD = new Vector(SIZE - 1, SIZE - 1);
    public static final Vector RADL = new Vector(SIZE, SIZE);
    private final String label;
    private Style onStyle;
    private IOState ioState;

    public LEDShape(String label, Color color) {
        this.label = label;
        onStyle = new Style(1, true, color);
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
        boolean fill = false;
        if (ioState != null)
            fill = ioState.getInput(0).getValue() != 0;

        Vector center = new Vector(2 + SIZE, 0);
        graphic.drawCircle(center.sub(RADL), center.add(RADL), Style.NORMAL);
        if (fill)
            graphic.drawCircle(center.sub(RAD), center.add(RAD), onStyle);
        Vector textPos = new Vector(SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.LEFTCENTER, Style.NORMAL);
    }
}
