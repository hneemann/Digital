package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Model;
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
public class ConstShape implements Shape {

    private long value;

    public ConstShape(long value) {
        this.value = value;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "out", Pin.Direction.output));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver, Model model) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, IOState ioState) {
        Vector textPos = new Vector(-3, 0);
        if (ioState != null)
            value = ioState.getOutput(0).getValue();
        graphic.drawText(textPos, textPos.add(1, 0), Long.toString(value), Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
