/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

/**
 * Used to visualize a missing shape
 */
public class MissingShape implements Shape {

    private final Pins pins = new Pins();
    private final Exception cause;
    private final String message;

    /**
     * Creates a new instance
     *
     * @param elementName the name of the element
     * @param cause       the cause of missing
     */
    public MissingShape(String elementName, Exception cause) {
        this.message = Lang.get("msg_missingShape_N", elementName);
        this.cause = cause;
    }

    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        Style style = Style.NORMAL_TEXT;
        graphic.drawText(new Vector(4, 4), message, Orientation.LEFTTOP, style);
        Throwable c = cause;
        int y = 4;
        while (c != null) {
            String message = c.getMessage();
            if (message != null && message.length() > 0) {
                if (message.length() > 100)
                    message = message.substring(0, 100) + "...";
                y += style.getFontSize();
                graphic.drawText(new Vector(4, y), message, Orientation.LEFTTOP, style);
            }
            c = c.getCause();
        }
    }
}
