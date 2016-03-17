package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.gui.draw.shapes.Shape;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author hneemann
 */
public class VisualPart implements Drawable, Moveable {
    private static final int PIN = 1;
    private final PartDescription partDescription;
    private transient GraphicMinMax minMax;
    private Vector pos;
    private int rotate;

    public VisualPart(PartDescription partDescription) {
        this.partDescription = partDescription;
        pos = new Vector(0, 0);
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

    public boolean matches(Vector min, Vector max) {
        GraphicMinMax m = getMinMax();
        return (min.x <= m.getMin().x) &&
                (m.getMax().x <= max.x) &&
                (min.y <= m.getMin().y) &&
                (m.getMax().y <= max.y);
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
        Graphic gr = new GraphicTransform(graphic, createTransform());
        Shape shape = partDescription.getShape();
        shape.drawTo(gr);
        for (Pin p : shape.getPins(partDescription))
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN), p.getDirection() == Pin.Direction.input ? Style.NORMAL : Style.FILLED);
    }

    private Transform createTransform() {
        return v -> v.add(pos);
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

    public ImageIcon createIcon(int maxHeight) {
        GraphicMinMax mm = getMinMax();

        if (mm.getMax().y - mm.getMin().y > maxHeight)
            return null;

        BufferedImage bi = new BufferedImage(mm.getMax().x - mm.getMin().x, mm.getMax().y - mm.getMin().y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bi.createGraphics();
        gr.setColor(new Color(255, 255, 255, 0));
        gr.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        gr.translate(-mm.getMin().x, -mm.getMin().y);
        GraphicSwing grs = new GraphicSwing(gr);
        drawTo(grs);
        return new ImageIcon(bi);
    }

    public PartDescription getPartDescription() {
        return partDescription;
    }

    public Pins getPins() {
        Shape shape = partDescription.getShape();
        Transform tr = createTransform();
        Pins pins = shape.getPins(partDescription);
        Pins transformed = new Pins();
        for (Pin p : pins)
            transformed.add(new Pin(tr.transform(p.getPos()), p));
        return transformed;
    }
}