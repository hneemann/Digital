package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;
import de.neemann.digital.gui.draw.parts.State;

/**
 * @author hneemann
 */
public class ConstShape implements Shape {

    public ConstShape() {
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "out", Pin.Direction.output));
    }

    @Override
    public Interactor applyStateMonitor(State state, Listener listener, Model model) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, State state) {
        Vector textPos = new Vector(-3, 0);
        long value = 1;
        if (state != null)
            value = state.getOutput(0).getValue();
        graphic.drawText(textPos, textPos.add(1, 0), Long.toString(value), Orientation.RIGHTCENTER);
    }
}
