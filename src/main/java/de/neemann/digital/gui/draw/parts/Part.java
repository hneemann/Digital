package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.GraphicTransform;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.gui.draw.shapes.Shape;

/**
 * @author hneemann
 */
public class Part implements Drawable, Moveable {
    private Shape shape;
    private Vector pos;
    private int rotate;

    public Part(Shape shape) {
        this.shape = shape;
    }

    public Vector getPos() {
        return pos;
    }

    public Part setPos(Vector pos) {
        this.pos = pos;
        return this;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    @Override
    public void drawTo(Graphic graphic) {
        Graphic gr = new GraphicTransform(graphic, pos, rotate);
        shape.drawTo(gr);
        for (Pin p : shape.getPins())
            gr.drawCircle(p.getPos().add(-1, -1), p.getPos().add(1, 1), Style.NORMAL);
    }

    @Override
    public void move(Vector delta) {
        pos = pos.add(delta);
    }
}
