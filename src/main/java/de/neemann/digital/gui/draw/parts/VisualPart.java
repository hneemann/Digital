package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.AttributeListener;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.gui.draw.shapes.Interactor;
import de.neemann.digital.gui.draw.shapes.Shape;
import de.neemann.digital.gui.draw.shapes.ShapeFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author hneemann
 */
public class VisualPart implements Drawable, Moveable, AttributeListener {
    private static final int PIN = 1;
    private final String partName;
    private final PartAttributes partAttributes;
    private transient GraphicMinMax minMax;
    private transient Shape shape;
    private transient IOState ioState;
    private transient Interactor interactor;
    private Vector pos;
    private int rotate;

    public VisualPart(String partName) {
        this.partName = partName;
        partAttributes = new PartAttributes();
        pos = new Vector(0, 0);
    }

    public String getPartName() {
        return partName;
    }

    public PartAttributes getPartAttributes() {
        partAttributes.addListener(this);
        return partAttributes;
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

    public Shape getShape() {
        if (shape == null)
            shape = ShapeFactory.INSTANCE.getShape(partName, partAttributes);
        return shape;
    }

    @Override
    public void drawTo(Graphic graphic, IOState ioState) {
        Graphic gr = new GraphicTransform(graphic, createTransform());
        Shape shape = getShape();
        shape.drawTo(gr, this.ioState);
        for (Pin p : shape.getPins())
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN), p.getDirection() == Pin.Direction.input ? Style.NORMAL : Style.FILLED);
    }

    private Transform createTransform() {
        return v -> v.add(pos);
    }

    public GraphicMinMax getMinMax() {
        if (minMax == null) {
            minMax = new GraphicMinMax();
            drawTo(minMax, ioState);
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
        drawTo(grs, ioState);
        return new ImageIcon(bi);
    }

    public Pins getPins() {
        Shape shape = getShape();
        Transform tr = createTransform();
        Pins pins = shape.getPins();
        Pins transformed = new Pins();
        for (Pin p : pins)
            transformed.add(new Pin(tr.transform(p.getPos()), p));
        return transformed;
    }

    /**
     * Sets the state of the parts inputs and outputs
     *
     * @param ioState    actual state
     * @param guiObserver can be used to update the GUI by calling hasChanged, maybe null
     * @param model    the model
     */
    public void setState(IOState ioState, Observer guiObserver, Model model) {
        this.ioState = ioState;
        if (ioState == null)
            interactor = null;
        else
            interactor = getShape().applyStateMonitor(ioState, guiObserver, model);
    }

    public void clicked(CircuitComponent cc, Vector pos) {
        if (interactor != null)
            interactor.interact(cc, pos, ioState);
    }

    @Override
    public void attributeChanged(AttributeKey key) {
        shape = null;
        minMax = null;
    }

    @Override
    public String toString() {
        String lab = partAttributes.get(AttributeKey.Label);
        if (lab != null && lab.length() > 0)
            return partName + "(" + lab + ")";
        else
            return partName;
    }
}