package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.core.PartFactory;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.GraphicTransform;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.gui.draw.shapes.Shape;

/**
 * @author hneemann
 */
public class VisualPart implements Drawable, Moveable {
    private static final int PIN = 1;
    private final PartFactory partFactory;
    private Shape shape;
    private Vector pos;
    private int rotate;

    public VisualPart(Shape shape, PartFactory partFactory) {
        this.shape = shape;
        this.partFactory = partFactory;
    }

    public Vector getPos() {
        return pos;
    }

    public VisualPart setPos(Vector pos) {
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
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN), p.getDirection() == Pin.Direction.input ? Style.NORMAL : Style.FILLED);
    }

    @Override
    public void move(Vector delta) {
        pos = pos.add(delta);
    }

    public PartFactory getPartFactory() {
        return partFactory;
    }
}
