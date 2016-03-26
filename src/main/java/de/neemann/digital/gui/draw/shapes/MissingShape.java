package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class MissingShape implements Shape {

    private final Pins pins = new Pins();
    private final String cause;
    private final String message;

    public MissingShape(String elementName, Exception cause) {
        this.message = Lang.get("msg_missingShape_N", elementName);
        this.cause = cause.getMessage();
    }

    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        Style style = Style.SHAPE_PIN;
        graphic.drawText(new Vector(0, 0), new Vector(1, 0), message, Orientation.LEFTBOTTOM, style);
        if (cause != null && cause.length() > 0)
            graphic.drawText(new Vector(0, style.getFontSize()), new Vector(1, style.getFontSize()), cause, Orientation.LEFTBOTTOM, style);
    }
}
