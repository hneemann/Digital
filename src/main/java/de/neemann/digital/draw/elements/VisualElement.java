package de.neemann.digital.draw.elements;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.shapes.*;
import de.neemann.digital.draw.shapes.Shape;
import de.neemann.digital.gui.components.CircuitComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * This class is used to store the visual representation of an element.
 * Instances of this class are also used to store a circuit to disk.
 *
 * @author hneemann
 */
public class VisualElement implements Drawable, Moveable, AttributeListener {
    private static final int PIN = 2;

    private transient GraphicMinMax minMax;
    private transient Shape shape;
    private transient IOState ioState;
    private transient InteractorInterface interactor;
    private transient Element element;
    private transient ShapeFactory shapeFactory;

    private final String elementName;
    private final ElementAttributes elementAttributes;
    private Vector pos;

    /**
     * creates a new instance
     * The name of the element is the name which is given to the Library to get the {@link de.neemann.digital.core.element.ElementTypeDescription}
     *
     * @param elementName the name of the element
     */
    public VisualElement(String elementName) {
        this.elementName = elementName;
        elementAttributes = new ElementAttributes();
        pos = new Vector(0, 0);
    }

    /**
     * Creates a copy of the given VisualElement
     *
     * @param proto the VisualElement to copy
     */
    public VisualElement(VisualElement proto) {
        this.elementName = proto.elementName;
        this.elementAttributes = new ElementAttributes(proto.elementAttributes);
        this.pos = new Vector(proto.pos);
    }

    /**
     * Returns the name of the element.
     * The name of the element is the name which is given to the Library to get the {@link de.neemann.digital.core.element.ElementTypeDescription}
     *
     * @return the name of the element
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @return the elements attributes
     */
    public ElementAttributes getElementAttributes() {
        elementAttributes.addListener(this);
        return elementAttributes;
    }

    /**
     * @return the position of this element
     */
    public Vector getPos() {
        return pos;
    }

    /**
     * Sets the position of this element
     *
     * @param pos the position
     * @return this for chained calls
     */
    public VisualElement setPos(Vector pos) {
        this.pos = pos;
        minMax = null;
        return this;
    }

    /**
     * Checks if the given point is within the bounding box of the shape of this element.
     *
     * @param p a position
     * @return true if p is inside the bounding box of the shape of this element.
     */
    public boolean matches(Vector p) {
        GraphicMinMax m = getMinMax();
        return (m.getMin().x <= p.x)
                && (m.getMin().y <= p.y)
                && (p.x <= m.getMax().x)
                && (p.y <= m.getMax().y);
    }

    /**
     * Checks if the given bounding box contains the bounding box of the shape of this element.
     *
     * @param min upper left corner of the bounding box
     * @param max lower right corner of the bounding box
     * @return true if the given box completely contains this element
     */
    public boolean matches(Vector min, Vector max) {
        GraphicMinMax m = getMinMax();
        return (min.x <= m.getMin().x)
                && (m.getMax().x <= max.x)
                && (min.y <= m.getMin().y)
                && (m.getMax().y <= max.y);
    }

    /**
     * @return the rotation of this element
     */
    public int getRotate() {
        return elementAttributes.get(Keys.ROTATE).getRotation();
    }

    /**
     * Returns the shape of this element.
     * The there is no shape the {@link ShapeFactory} is requested for the shape.
     *
     * @return the shape
     */
    public Shape getShape() {
        if (shape == null) {
            shape = shapeFactory.getShape(elementName, elementAttributes);
            minMax = null;
        }
        return shape;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        drawShape(graphic, highLight);

        // draw circle around element
        if (highLight) {
            GraphicMinMax mm = getMinMax();
            Vector delta = mm.getMax().sub(mm.getMin()).add(SIZE, SIZE).div(2);
            Vector pos = mm.getMax().add(mm.getMin()).div(2);
            graphic.drawCircle(pos.sub(delta), pos.add(delta), Style.HIGHLIGHT);
        }
    }

    private void drawShape(Graphic graphic, boolean highLight) {
        Graphic gr = new GraphicTransform(graphic, createTransform());
        Shape shape = getShape();
        shape.drawTo(gr, highLight);
        for (Pin p : shape.getPins())
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN),
                    p.getDirection() == Pin.Direction.input ? Style.WIRE : Style.WIRE_OUT);
    }

    private Transform createTransform() {
        int rotate = getRotate();
        if (rotate == 0)
            return v -> v.add(pos);
        else
            return new TransformRotate(pos, rotate);
    }

    /**
     * @return the bounding box of the shape of this element, text is ignored
     */
    public GraphicMinMax getMinMax() {
        if (minMax == null) {
            GraphicMinMax mm = new GraphicMinMax(false);
            drawShape(mm, false);
            minMax = mm;
        }
        return minMax;
    }

    @Override
    public void move(Vector delta) {
        pos = pos.add(delta);
        minMax = null;
    }

    /**
     * Create an icon from this element.
     * Is used to create the icons in the element menu
     *
     * @param maxHeight the maximum height
     * @return the created icon
     */
    public ImageIcon createIcon(int maxHeight) {
        GraphicMinMax mm = new GraphicMinMax();
        drawShape(mm, false);

        double scale = 0.5;
        if (mm.getMax().y - mm.getMin().y > maxHeight / scale)
            scale = (double) (maxHeight - 1) / (mm.getMax().y - mm.getMin().y + 4);

        int width = (int) Math.round((mm.getMax().x - mm.getMin().x + 4) * scale + 1);
        int height = (int) Math.round((mm.getMax().y - mm.getMin().y + 4) * scale + 1);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bi.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        gr.setColor(new Color(255, 255, 255, 0));
        gr.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        gr.scale(scale, scale);
        gr.translate(2 - mm.getMin().x, 2 - mm.getMin().y);
        GraphicSwing grs = new GraphicSwing(gr);
        drawTo(grs, false);
        return new ImageIcon(bi);
    }

    /**
     * @return the pins of this element
     */
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
     * @param ioState     actual state, if null VisualPart is reseted
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

    /**
     * Is called if this element is clicked with the mouse.
     * The call is delegated to the {@link Interactor} of the {@link Shape}
     *
     * @param cc  the calling {@link CircuitComponent}
     * @param pos the position
     * @return true if model is changed
     */
    public boolean elementClicked(CircuitComponent cc, Point pos) {
        if (interactor != null)
            return interactor.clicked(cc, pos, ioState, element);
        else
            return false;
    }

    /**
     * Is called if this element is clicked with the mouse.
     * The call is delegated to the {@link Interactor} of the {@link Shape}
     *
     * @param cc  the calling {@link CircuitComponent}
     * @param pos the position
     * @return true if model is changed
     */
    public boolean elementPressed(CircuitComponent cc, Point pos) {
        if (interactor != null)
            return interactor.pressed(cc, pos, ioState, element);
        else
            return false;
    }

    /**
     * Is called if this element is clicked with the mouse.
     * The call is delegated to the {@link Interactor} of the {@link Shape}
     *
     * @param cc  the calling {@link CircuitComponent}
     * @param pos the position
     * @return true if model is changed
     */
    public boolean elementReleased(CircuitComponent cc, Point pos) {
        if (interactor != null)
            return interactor.released(cc, pos, ioState, element);
        else
            return false;
    }


    @Override
    public void attributeChanged(Key key) {
        shape = null;
    }

    @Override
    public String toString() {
        String lab = elementAttributes.getCleanLabel();
        if (lab != null && lab.length() > 0)
            return elementName + "(" + lab + ")";
        else
            return elementName;
    }

    /**
     * Sets the concrete element created.
     * The value is given to the {@link Interactor} if the shape is clicked.
     *
     * @param element the element
     */
    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * Sets the shape factory of this element.
     *
     * @param shapeFactory the {@link ShapeFactory}
     * @return this for chained calls
     */
    public VisualElement setShapeFactory(ShapeFactory shapeFactory) {
        this.shapeFactory = shapeFactory;
        return this;
    }

    /**
     * Returns true if this element is from the given type
     *
     * @param description the description of the type
     * @return true if element is of the given type
     */
    public boolean equalsDescription(ElementTypeDescription description) {
        return elementName.equals(description.getName());
    }

    /**
     * Rotates the element
     */
    public void rotate() {
        int rotate = getRotate();
        rotate += 1;
        if (rotate > 3) rotate -= 4;
        elementAttributes.set(Keys.ROTATE, new Rotation(rotate));
        minMax = null;
    }
}
