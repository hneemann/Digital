package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.gui.draw.shapes.Shape;

/**
 * @author hneemann
 */
public class VisualPart implements Drawable, Moveable {
    private static final int PIN = 1;
    private final PartDescription partDescription;
    private transient GraphicMinMax minMax;
    private Shape shape;
    private Vector pos;
    private int rotate;

    public VisualPart(Shape shape, PartDescription partDescription) {
        this.shape = shape;
        this.partDescription = partDescription;
    }

    public Vector getPos() {
        return pos;
    }

    public VisualPart setPos(Vector pos) {
        this.pos = pos;
        minMax = null;
        return this;
    }

    public boolean matches(Vector p) {
        GraphicMinMax m = getMinMax();
        return (m.getMin().x <= p.x) &&
                (m.getMin().y <= p.y) &&
                (p.x <= m.getMax().x) &&
                (p.y <= m.getMax().y);
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
        minMax = null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        Graphic gr = new GraphicTransform(graphic, pos, rotate);
        shape.drawTo(gr);
        for (Pin p : shape.getPins())
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN), p.getDirection() == Pin.Direction.input ? Style.NORMAL : Style.FILLED);
    }

    public GraphicMinMax getMinMax() {
        if (minMax == null) {
            minMax = new GraphicMinMax();
            drawTo(minMax);
        }
        return minMax;
    }

    @Override
    public void move(Vector delta) {
        pos = pos.add(delta);
        minMax = null;
    }

    public PartDescription getPartDescription() {
        return partDescription;
    }
}