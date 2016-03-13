package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;

/**
 * @author hneemann
 */
public class Wire implements Drawable, Moveable {

    private Vector p1;
    private Vector p2;

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawLine(p1, p2, Style.WIRE);
    }

    @Override
    public void move(Vector delta) {
        p1 = p1.add(delta);
        p2 = p2.add(delta);
    }
}
