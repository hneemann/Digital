package de.neemann.digital.draw.elements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.lang.Lang;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author hneemann
 */
public class Circuit {
    private static final Set<Drawable> EMPTY_SET = Collections.emptySet();
    private static final ArrayList<AttributeKey> ATTR_LIST = new ArrayList<>();

    static {
        ATTR_LIST.add(AttributeKey.Width);
    }

    private int version = 1;
    private ElementAttributes attributes;
    private final ArrayList<VisualElement> visualElements;
    private ArrayList<Wire> wires;
    private List<String> measurementOrdering;
    private transient boolean dotsPresent = false;
    private transient boolean modified = false;

    private static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("visualElement", VisualElement.class);
        xStream.alias("wire", Wire.class);
        xStream.alias("circuit", Circuit.class);
        xStream.alias("rotation", Rotation.class);
        xStream.aliasAttribute(Rotation.class, "rotation", "rotation");
        xStream.alias("vector", Vector.class);
        xStream.aliasAttribute(Vector.class, "x", "x");
        xStream.aliasAttribute(Vector.class, "y", "y");
        xStream.addImplicitCollection(ElementAttributes.class, "attributes");
        xStream.alias("data", DataField.class);
        xStream.addImplicitCollection(DataField.class, "data");
        return xStream;
    }

    /**
     * Creates a new circuit instance from a stored file
     *
     * @param filename     filename
     * @param shapeFactory shapeFactory used to create the shapes
     * @return the circuit
     * @throws IOException IOException
     */
    public static Circuit loadCircuit(File filename, ShapeFactory shapeFactory) throws IOException {
        XStream xStream = getxStream();
        try (InputStream in = new FileInputStream(filename)) {
            Circuit circuit = (Circuit) xStream.fromXML(in);
            for (VisualElement ve : circuit.getElements())
                ve.setShapeFactory(shapeFactory);

            if (circuit.version == 0) {
                // convert to version 1
                for (Wire w : circuit.getWires()) {
                    w.p1 = w.p1.mul(2);
                    w.p2 = w.p2.mul(2);
                }
                for (VisualElement e : circuit.getElements())
                    e.setPos(e.getPos().mul(2));
                circuit.version = 1;
            }

            return circuit;
        }
    }

    /**
     * Stores the circuit in the given file
     *
     * @param filename filename
     * @throws IOException IOException
     */
    public void save(File filename) throws IOException {
        wires = new WireConsistencyChecker(wires).check();
        dotsPresent = false;
        XStream xStream = Circuit.getxStream();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(filename), "utf-8")) {
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(this, new PrettyPrintWriter(out));
            modified = false;
        }
    }

    /**
     * Creates a ne empty circuit instance
     */
    public Circuit() {
        visualElements = new ArrayList<>();
        wires = new ArrayList<>();
    }

    /**
     * returns the elements attributes
     *
     * @return the attributes
     */
    public ElementAttributes getAttributes() {
        if (attributes == null)
            attributes = new ElementAttributes();
        return attributes;
    }

    /**
     * Opens the attribute editor
     *
     * @param parent the parent component
     */
    public void editAttributes(Component parent) {
        if (new AttributeDialog(parent, ATTR_LIST, getAttributes()).showDialog()) {
            if (attributes.isEmpty())
                attributes = null;
            modified();
        }
    }

    /**
     * Draws tis circuit using the given graphic instance
     *
     * @param graphic the graphic instance used
     */
    public void drawTo(Graphic graphic) {
        drawTo(graphic, EMPTY_SET);
    }

    /**
     * Draws tis circuit using the given graphic instance
     *
     * @param graphic     the graphic instance used
     * @param highLighted a list of Drawables to highlight
     */
    public void drawTo(Graphic graphic, Collection<Drawable> highLighted) {
        if (!dotsPresent) {
            new DotCreator(wires).applyDots();
            dotsPresent = true;
        }

        graphic.openGroup();
        for (Wire w : wires)
            w.drawTo(graphic, highLighted.contains(w));
        graphic.closeGroup();
        for (VisualElement p : visualElements) {
            graphic.openGroup();
            p.drawTo(graphic, highLighted.contains(p));
            graphic.closeGroup();
        }
    }

    /**
     * Adds a ne VisualElement
     *
     * @param visualElement the visual element to add
     */
    public void add(VisualElement visualElement) {
        visualElements.add(visualElement);
        modified();
    }

    /**
     * Adds a new Wire
     *
     * @param newWire the wire to add
     */
    public void add(Wire newWire) {
        if (newWire.p1.equals(newWire.p2))
            return;

        int len = wires.size();
        for (int i = 0; i < len; i++) {
            Wire present = wires.get(i);
            if (present.contains(newWire.p1)) {
                wires.set(i, new Wire(present.p1, newWire.p1));
                wires.add(new Wire(present.p2, newWire.p1));
            } else if (present.contains(newWire.p2)) {
                wires.set(i, new Wire(present.p1, newWire.p2));
                wires.add(new Wire(present.p2, newWire.p2));
            }
        }

        wires.add(newWire);
        WireConsistencyChecker checker = new WireConsistencyChecker(wires);
        wires = checker.check();

        dotsPresent = false;
        modified();
    }

    /**
     * Returns a list of all visual elements
     *
     * @return the list
     */
    public ArrayList<VisualElement> getElements() {
        return visualElements;
    }

    /**
     * Returns a list of all Moveables in the given rectangle.
     *
     * @param min upper left corner of the rectangle
     * @param max lower right corner of the rectangle
     * @return the list
     */
    public ArrayList<Moveable> getElementsToMove(Vector min, Vector max) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualElement vp : visualElements)
            if (vp.matches(min, max))
                m.add(vp);

        for (Wire w : wires) {
            if (w.p1.inside(min, max))
                m.add(w.getMovableP1());
            if (w.p2.inside(min, max))
                m.add(w.getMovableP2());
        }
        if (m.isEmpty())
            return null;
        else
            return m;
    }

    /**
     * Returns a list of all Moveables in the given rectangle.
     * It creates a deep copy of all elements.
     *
     * @param min upper left corner of the rectangle
     * @param max lower right corner of the rectangle
     * @return the list
     */
    public ArrayList<Moveable> getElementsToCopy(Vector min, Vector max, ShapeFactory shapeFactory) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualElement vp : visualElements)
            if (vp.matches(min, max))
                m.add(new VisualElement(vp).setShapeFactory(shapeFactory));

        for (Wire w : wires)
            if (w.p1.inside(min, max) && w.p2.inside(min, max))
                m.add(new Wire(w));

        if (m.isEmpty())
            return null;
        else
            return m;
    }


    /**
     * Deletes all elements th the given rectangle
     *
     * @param min upper left corner of the rectangle
     * @param max lower right corner of the rectangle
     */
    public void delete(Vector min, Vector max) {
        Iterator<VisualElement> veIt = visualElements.iterator();
        while (veIt.hasNext())
            if (veIt.next().matches(min, max))
                veIt.remove();

        Iterator<Wire> wIt = wires.iterator();
        while (wIt.hasNext()) {
            Wire w = wIt.next();
            if (w.p1.inside(min, max) || w.p2.inside(min, max))
                wIt.remove();
        }
        dotsPresent = false;
        modified();
    }

    /**
     * Deletes a single visual element
     *
     * @param partToDelete the element to delete
     */
    public void delete(VisualElement partToDelete) {
        if (visualElements.remove(partToDelete))
            modified();
    }


    /**
     * Returns the element at the given position
     *
     * @param pos the cursor position
     * @return the element or null if there is no element at the given position
     */
    public VisualElement getElementAt(Vector pos) {
        for (VisualElement element : visualElements) {
            if (element.matches(pos))
                return element;
        }
        return null;
    }

    /**
     * Returns true if there is a pin at the given position
     *
     * @param pos the position
     * @return true if position is a pin position
     */
    public boolean isPinPos(Vector pos) {
        VisualElement el = getElementAt(pos);
        if (el == null) return false;

        return isPinPos(pos, el);
    }

    /**
     * Returns true if the given element has a pin at the given position
     *
     * @param pos the position
     * @param el  the element
     * @return true if position is a pin position
     */
    public boolean isPinPos(Vector pos, VisualElement el) {
        for (Pin p : el.getPins())
            if (p.getPos().equals(pos))
                return true;

        return false;
    }


    /**
     * Sets this circuits state to modified
     */
    public void modified() {
        modified = true;
    }

    /**
     * @return true if modified
     */
    public boolean isModified() {
        return modified;
    }


    /**
     * @return a list of all wires
     */
    public ArrayList<Wire> getWires() {
        return wires;
    }

    /**
     * Deletes the references to the ObservableValues representing the elements or wire state.
     */
    public void clearState() {
        for (VisualElement vp : visualElements)
            vp.setState(null, null);
        for (Wire w : wires)
            w.setValue(null);
    }

    /**
     * returns a list of all input names of this circuit
     *
     * @param library the library
     * @return the list of input names
     * @throws PinException PinException
     */
    public String[] getInputNames(ElementLibrary library) throws PinException {
        ArrayList<String> pinList = new ArrayList<>();
        for (VisualElement ve : visualElements) {
            ElementTypeDescription elementType = library.getElementType(ve.getElementName());
            if (elementType == In.DESCRIPTION) {
                String name = ve.getElementAttributes().get(AttributeKey.Label);
                if (name == null || name.length() == 0)
                    throw new PinException(Lang.get("err_pinWithoutName"));

                pinList.add(name);
            }
        }
        return pinList.toArray(new String[pinList.size()]);
    }

    /**
     * returns a list of all output ObservableNames.
     * The ObservableValue is not connected to a model! Its just a wrapper for the outputs name.
     * This method is used to create dummy outputs for a nested element.
     * They are not used, because during creation of a model all inputs and outputs which connect a model
     * with a nested model are removed from the model and replaced by a direct connection of inputs and outputs.
     *
     * @param library the library
     * @return the list of output ObservableValues
     * @throws PinException PinException
     */
    public ObservableValue[] getOutputNames(ElementLibrary library) throws PinException {
        ArrayList<ObservableValue> pinList = new ArrayList<>();
        for (VisualElement ve : visualElements) {
            ElementTypeDescription elementType = library.getElementType(ve.getElementName());
            if (elementType == Out.DESCRIPTION) {
                String name = ve.getElementAttributes().get(AttributeKey.Label);
                if (name == null || name.length() == 0)
                    throw new PinException(Lang.get("err_pinWithoutName"));

                pinList.add(new ObservableValue(name, 0) {
                    @Override
                    public long getValue() {
                        throw new RuntimeException("invalid call!");
                    }

                    @Override
                    public ObservableValue addObserverToValue(Observer observer) {
                        throw new RuntimeException("invalid call!");
                    }
                });
            }
        }
        return pinList.toArray(new ObservableValue[pinList.size()]);
    }

    /**
     * Gets the ordering of values used to show measurements
     *
     * @return list of names
     */
    public List<String> getMeasurementOrdering() {
        return measurementOrdering;
    }

    /**
     * Sets the ordering of values used to show measurements
     *
     * @param measurementOrdering
     */
    public void setMeasurementOrdering(List<String> measurementOrdering) {
        this.measurementOrdering = measurementOrdering;
    }
}
