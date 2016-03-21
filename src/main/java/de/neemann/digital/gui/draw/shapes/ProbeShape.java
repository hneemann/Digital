package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;

/**
 * @author hneemann
 */
public class ProbeShape implements Shape {

    private final String label;
    private IOState ioState;
    private int bits;

    public ProbeShape(String label) {
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
        bits = ioState.getInput(0).getBits();
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawText(new Vector(2, -1), new Vector(3, -1), label, Orientation.LEFTBOTTOM, Style.NORMAL);
        if (bits > 1) {
            String v = ioState.getInput(0).getValueString();
            graphic.drawText(new Vector(2, 1), new Vector(3, 1), v, Orientation.LEFTTOP, Style.NORMAL);
        }
    }
}
