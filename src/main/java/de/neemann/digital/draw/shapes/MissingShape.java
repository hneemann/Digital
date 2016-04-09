package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
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
    public void drawTo(Graphic graphic, boolean highLight) {
        Style style = Style.SHAPE_PIN;
        graphic.drawLine(new Vector(0, 0), new Vector(style.getFontSize() * 10, 0), style);
        graphic.drawLine(new Vector(0, 0), new Vector(0, style.getFontSize() * 2), style);
        graphic.drawText(new Vector(4, 4), new Vector(5, 4), message, Orientation.LEFTTOP, style);
        if (cause != null && cause.length() > 0)
            graphic.drawText(new Vector(4, 4 + style.getFontSize()), new Vector(5, 4 + style.getFontSize()), cause, Orientation.LEFTTOP, style);
    }
}
