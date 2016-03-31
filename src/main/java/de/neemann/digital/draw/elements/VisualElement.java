package de.neemann.digital.draw.elements;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.AttributeListener;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.Interactor;
import de.neemann.digital.draw.shapes.Shape;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author hneemann
 */
public class VisualElement implements Drawable, Moveable, AttributeListener {
    private static final int PIN = 1;
    private final String elementName;
    private final ElementAttributes elementAttributes;
    private transient GraphicMinMax minMax;
    private transient Shape shape;
    private transient IOState ioState;
    private transient Interactor interactor;
    private transient Element element;
    private transient ShapeFactory shapeFactory;
    private Vector pos;
    private int rotate;

    public VisualElement(String elementName) {
        this.elementName = elementName;
        elementAttributes = new ElementAttributes();
        pos = new Vector(0, 0);
    }

    public VisualElement(VisualElement proto) {
        this.elementName = proto.elementName;
        this.elementAttributes = new ElementAttributes(proto.elementAttributes);
        this.pos = new Vector(proto.pos);
        this.rotate = proto.rotate;
    }

    public String getElementName() {
        return elementName;
    }

    public ElementAttributes getElementAttributes() {
        elementAttributes.addListener(this);
        return elementAttributes;
    }

    public Vector getPos() {
        return pos;
    }

    public VisualElement setPos(Vector pos) {
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
            shape = shapeFactory.getShape(elementName, elementAttributes);
        return shape;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        Graphic gr = new GraphicTransform(graphic, createTransform());
        Shape shape = getShape();

        shape.drawTo(gr, highLight);
        for (Pin p : shape.getPins())
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN)
                    , p.getDirection() == Pin.Direction.input ? Style.WIRE : Style.WIRE_OUT);

        if (highLight && minMax == null && !(graphic instanceof GraphicMinMax)) getMinMax();

        if (highLight && minMax != null) {
            Vector delta = minMax.getMax().sub(minMax.getMin());
            int rad = (int) Math.sqrt(delta.x * delta.x + delta.y * delta.y) / 2;
            delta = new Vector(rad, rad);
            Vector pos = minMax.getMax().add(minMax.getMin()).div(2);
            graphic.drawCircle(pos.sub(delta), pos.add(delta), Style.HIGHLIGHT);
        }
    }

    private Transform createTransform() {
        if (rotate == 0)
            return v -> v.add(pos);
        else
            return new TransformRotate(pos, rotate);
    }

    public GraphicMinMax getMinMax() {
        if (minMax == null) {
            GraphicMinMax mm = new GraphicMinMax();
            drawTo(mm, false);
            minMax = mm;
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

        BufferedImage bi = new BufferedImage(mm.getMax().x - mm.getMin().x + 4, mm.getMax().y - mm.getMin().y + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bi.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        gr.setColor(new Color(255, 255, 255, 0));
        gr.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        gr.translate(2 - mm.getMin().x, 2 - mm.getMin().y);
        GraphicSwing grs = new GraphicSwing(gr);
        drawTo(grs, false);
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
     * Sets the state of the elements inputs and outputs
     *
     * @param ioState     actual state, if null VisualPart is reset
     * @param guiObserver can be used to update the GUI by calling hasChanged, maybe null
     */
    public void setState(IOState ioState, Observer guiObserver) {
        this.ioState = ioState;
        if (ioState == null) {
            interactor = null;
            shape = null;
        } else
            interactor = getShape().applyStateMonitor(ioState, guiObserver);
    }

    public void clicked(CircuitComponent cc, Point pos) {
        if (interactor != null)
            interactor.clicked(cc, pos, ioState, element);
    }

    @Override
    public void attributeChanged(AttributeKey key) {
        shape = null;
        minMax = null;
        rotate = elementAttributes.get(AttributeKey.Rotate).rotation;
    }

    @Override
    public String toString() {
        String lab = elementAttributes.get(AttributeKey.Label);
        if (lab != null && lab.length() > 0)
            return elementName + "(" + lab + ")";
        else
            return elementName;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public VisualElement setShapeFactory(ShapeFactory shapeFactory) {
        this.shapeFactory = shapeFactory;
        return this;
    }
}