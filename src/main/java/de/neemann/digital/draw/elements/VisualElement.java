/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.shapes.*;
import de.neemann.digital.draw.shapes.Shape;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * This class is used to store the visual representation of an element.
 * Instances of this class are also used to store a circuit to disk.
 */
public class VisualElement implements Drawable, Movable, AttributeListener {
    private static final int PIN = 2;

    private transient GraphicMinMax minMax;
    private transient GraphicMinMax minMaxText;
    private transient IOState ioState;
    private transient InteractorInterface interactor;
    private transient Element element;
    // shape is set to null and recreated if needed if attributes are changed
    private transient Shape shape;
    // shapes are recreated if attributes are changed, therefore a factory is necessary and not only a simple shape!
    private transient ShapeFactory shapeFactory;
    private transient Transform transform;

    // these fields are stored to disk
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
        setPos(new Vector(0, 0));
    }

    /**
     * Creates a copy of the given VisualElement
     *
     * @param proto the VisualElement to copy
     */
    public VisualElement(VisualElement proto) {
        this.elementName = proto.elementName;
        this.elementAttributes = new ElementAttributes(proto.elementAttributes);
        setPos(new Vector(proto.pos));
        this.shapeFactory = proto.shapeFactory;
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
     * Sets a key.
     * Use only to construct an instance.
     * Don't use this function to modify an existing instance!
     *
     * @param key the key
     * @param val the value to set
     * @param <V> the type of the value
     * @return this for chained calls
     */
    public <V> VisualElement setAttribute(Key<V> key, V val) {
        elementAttributes.set(key, val);
        return this;
    }

    /**
     * @return the elements attributes
     */
    public ElementAttributes getElementAttributes() {
        elementAttributes.addListener(this);
        return elementAttributes;
    }

    @Override
    public void attributeChanged() {
        shape = null;
        resetGeometry();
    }

    private void resetGeometry() {
        transform = null;
        minMax = null;
        minMaxText = null;
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
        resetGeometry();
        return this;
    }

    /**
     * Checks if the given point is within the bounding box of the shape of this element.
     *
     * @param p           a position
     * @param includeText true if a click on a text also selectes the element
     * @return true if p is inside the bounding box of the shape of this element.
     */
    public boolean matches(Vector p, boolean includeText) {
        GraphicMinMax m = getMinMax(includeText);
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
        GraphicMinMax m = getMinMax(false);
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
            resetGeometry();
        }
        return shape;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        drawShape(graphic, highLight);

        // draw circle around element
        if (highLight != null) {
            GraphicMinMax mm = getMinMax(false);
            Vector delta = mm.getMax().sub(mm.getMin()).add(SIZE, SIZE).div(2);
            Vector pos = mm.getMax().add(mm.getMin()).div(2);
            graphic.drawCircle(pos.sub(delta), pos.add(delta), highLight);
        }
    }

    private void drawShape(Graphic graphic, Style highLight) {
        Graphic gr = new GraphicTransform(graphic, createTransform());
        Shape shape = getShape();
        shape.drawTo(gr, highLight);
        for (Pin p : shape.getPins())
            gr.drawCircle(p.getPos().add(-PIN, -PIN), p.getPos().add(PIN, PIN),
                    p.getDirection() == Pin.Direction.input ? Style.WIRE : Style.WIRE_OUT);
    }

    private Transform createTransform() {
        if (transform == null) {
            int rotate = getRotate();
            if (rotate == 0)
                transform = new TransformTranslate(pos);
            else
                transform = new TransformRotate(pos, rotate);
        }
        return transform;
    }

    /**
     * @param includeText true if a click on a text also selectes the element
     * @return the bounding box of the shape of this element, text is ignored
     */
    public GraphicMinMax getMinMax(boolean includeText) {
        if (includeText) {
            if (minMaxText == null) {
                GraphicMinMax mm = new GraphicMinMax(true, null);
                drawShape(mm, null);
                minMaxText = mm;
            }
            return minMaxText;
        } else {
            if (minMax == null) {
                GraphicMinMax mm = new GraphicMinMax(false, null);
                drawShape(mm, null);
                minMax = mm;
            }
            return minMax;
        }
    }

    @Override
    public void move(Vector delta) {
        setPos(pos.add(delta));
    }

    /**
     * Create an icon from this element.
     * Is used to create the icons in the element menu
     *
     * @param maxHeight the maximum height
     * @return the created icon
     */
    public ImageIcon createIcon(int maxHeight) {
        float scaling = Screen.getInstance().getScaling();
        BufferedImage bi = getBufferedImage(0.5 * scaling, (int) (maxHeight * scaling));
        return new ImageIcon(bi);
    }

    /**
     * Creates an image representing this element
     *
     * @param scale     the scaling
     * @param maxHeight th maximal height
     * @return the BufferedImage
     */
    public BufferedImage getBufferedImage(double scale, int maxHeight) {
        GraphicMinMax mm = new GraphicMinMax();
        drawShape(mm, null);

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
        drawTo(grs, null);
        return bi;
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
            transformed.add(new Pin(tr.transform(p.getPos()), p).setVisualElement(this));
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
     * @param cc             the calling {@link CircuitComponent}
     * @param pos            the position
     * @param posInComponent position in CircuitComponent
     * @param modelSync      used to access the running model
     * @return true if model is changed
     */
    public boolean elementClicked(CircuitComponent cc, Point pos, Vector posInComponent, SyncAccess modelSync) {
        if (interactor != null)
            return interactor.clicked(cc, pos, ioState, element, modelSync);
        else
            return false;
    }

    /**
     * Is called if this element is clicked with the mouse.
     * The call is delegated to the {@link Interactor} of the {@link Shape}
     *
     * @param cc             the calling {@link CircuitComponent}
     * @param pos            the position
     * @param posInComponent position in CircuitComponent
     * @param modelSync      used to access the running model
     * @return true if model is changed
     */
    public boolean elementPressed(CircuitComponent cc, Point pos, Vector posInComponent, SyncAccess modelSync) {
        if (interactor != null)
            return interactor.pressed(cc, pos, ioState, element, modelSync);
        else
            return false;
    }

    /**
     * Is called if this element is clicked with the mouse.
     * The call is delegated to the {@link Interactor} of the {@link Shape}
     *
     * @param cc             the calling {@link CircuitComponent}
     * @param pos            the position
     * @param posInComponent position in CircuitComponent
     * @param modelSync      used to access the running model
     * @return true if model is changed
     */
    public boolean elementReleased(CircuitComponent cc, Point pos, Vector posInComponent, SyncAccess modelSync) {
        if (interactor != null)
            return interactor.released(cc, pos, ioState, element, modelSync);
        else
            return false;
    }

    /**
     * Is called if the mouse is dragged on this element.
     * The call is delegated to the {@link Interactor} of the {@link Shape}
     *
     * @param cc             the calling {@link CircuitComponent}
     * @param pos            the position
     * @param posInComponent position in CircuitComponent
     * @param modelSync      used to access the running model
     * @return true if model is changed
     */
    public boolean elementDragged(CircuitComponent cc, Point pos, Vector posInComponent, SyncAccess modelSync) {
        if (interactor != null)
            return interactor.dragged(cc, posInComponent, transform, ioState, element, modelSync);
        else
            return false;
    }


    @Override
    public String toString() {
        String lab = elementAttributes.getCleanLabel();
        if (lab != null && lab.length() > 0)
            return elementName + " (" + lab + ")";
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
        getElementAttributes().set(Keys.ROTATE, new Rotation(rotate));
    }

    /**
     * Set the orientation of the element.
     *
     * @param rotation rotation
     * @return this for chained calls
     */
    public VisualElement setRotation(int rotation) {
        getElementAttributes().set(Keys.ROTATE, new Rotation(rotation));
        return this;
    }

    /**
     * @return true if one can interact with this element
     */
    public boolean isInteractive() {
        return interactor != null;
    }

}
