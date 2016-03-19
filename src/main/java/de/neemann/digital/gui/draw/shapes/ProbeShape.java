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
public class ProbeShape implements Shape {

    private final String label;

    public ProbeShape(String label) {
        this.label = label;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "in", Pin.Direction.input));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver, Model model) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, IOState ioState) {
        graphic.drawText(new Vector(2, -1), new Vector(3, -1), label, Orientation.LEFTBOTTOM, Style.NORMAL);
    }
}
