package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;

/**
 * @author hneemann
 */
public class Wire implements Drawable, Moveable {

    public Vector p1;
    public Vector p2;

    public Wire(Vector p1, Vector p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawLine(p1, p2, Style.WIRE);
    }

    @Override
    public void move(Vector delta) {
        p1 = p1.add(delta);
        p2 = p2.add(delta);
    }

    public void setP2(Vector p2) {
        this.p2 = p2;
    }
}
