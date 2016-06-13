package de.neemann.digital.draw.elements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.IntFormat;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.language.Language;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class Circuit {
    private static final Set<Drawable> EMPTY_SET = Collections.emptySet();
    private static final ArrayList<Key> ATTR_LIST = new ArrayList<>();

    static {
        ATTR_LIST.add(Keys.WIDTH);
    }

    private int version = 1;
    private ElementAttributes attributes;
    private final ArrayList<VisualElement> visualElements;
    private ArrayList<Wire> wires;
    private List<String> measurementOrdering;
    private transient boolean dotsPresent = false;
    private transient boolean modified = false;

    /**
     * Creates a proper configurated XStream instance
     *
     * @return the XStream instance
     */
    public static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("attributes", ElementAttributes.class);
        xStream.alias("visualElement", VisualElement.class);
        xStream.alias("wire", Wire.class);
        xStream.alias("circuit", Circuit.class);
        xStream.alias("intFormat", IntFormat.class);
        xStream.alias("rotation", Rotation.class);
        xStream.aliasAttribute(Rotation.class, "rotation", "rotation");
        xStream.alias("language", Language.class);
        xStream.aliasAttribute(Language.class, "name", "name");
        xStream.alias("vector", Vector.class);
        xStream.aliasAttribute(Vector.class, "x", "x");
        xStream.aliasAttribute(Vector.class, "y", "y");
        xStream.addImplicitCollection(ElementAttributes.class, "attributes");
        xStream.alias("data", DataField.class);
        xStream.addImplicitCollection(DataField.class, "data");
        xStream.ignoreUnknownElements();
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

        wires.add(newWire);
        WireConsistencyChecker checker = new WireConsistencyChecker(wires);
        wires = checker.check();

        dotsPresent = false;
        modified();
    }

    /**
     * Called if elements are moved
     */
    public void elementsMoved() {
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
     * Returns a list of all Drawables in the given rectangle.
     *
     * @param min upper left corner of the rectangle
     * @param max lower right corner of the rectangle
     * @return the list
     */
    public ArrayList<Drawable> getElementsToHighlight(Vector min, Vector max) {
        ArrayList<Drawable> m = new ArrayList<>();
        for (VisualElement vp : visualElements)
            if (vp.matches(min, max))
                m.add(vp);

        for (Wire w : wires) {
            if (w.p1.inside(min, max) || w.p2.inside(min, max))
                m.add(w);
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
     * Deletes all elements in the given rectangle
     *
     * @param min upper left corner of the rectangle
     * @param max lower right corner of the rectangle
     */
    public void delete(Vector min, Vector max) {
        Iterator<VisualElement> veIt = visualElements.iterator();
        while (veIt.hasNext())
            if (veIt.next().matches(min, max))
                veIt.remove();

        boolean wireDeleted = false;
        Iterator<Wire> wIt = wires.iterator();
        while (wIt.hasNext()) {
            Wire w = wIt.next();
            if (w.p1.inside(min, max) || w.p2.inside(min, max)) {
                wIt.remove();
                wireDeleted = true;
            }
        }

        if (wireDeleted) {
            WireConsistencyChecker checker = new WireConsistencyChecker(wires);
            wires = checker.check();
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
     * Deletes a single wire
     *
     * @param wireToDelete the wire to delete
     */
    public void delete(Wire wireToDelete) {
        if (wires.remove(wireToDelete)) {
            WireConsistencyChecker checker = new WireConsistencyChecker(wires);
            wires = checker.check();
            dotsPresent = false;
            modified();
        }
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
        return getPinAt(pos, el) != null;
    }

    /**
     * Returns the pin at the given position
     *
     * @param pos position
     * @param el  the element
     * @return the pin or null if no pin found
     */
    public Pin getPinAt(Vector pos, VisualElement el) {
        for (Pin p : el.getPins())
            if (p.getPos().equals(pos))
                return p;

        return null;
    }


    /**
     * Sets this circuits state to modified
     */
    public void modified() {
        modified = true;
    }

    /**
     * Sets this circuits state to not modified
     */
    public void setNotModified() {
        modified = false;
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
     * Returns the matching wire
     *
     * @param pos the position
     * @return the matching wire or null
     */
    public Wire getWireAt(Vector pos, int radius) {
        for (Wire w : wires)
            if (w.contains(pos, radius))
                return w;
        return null;
    }


    /**
     * Deletes the references to the ObservableValues representing the elements or wire state.
     * So this circuit is detached from a generated model.
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
     * @return the list of input names
     * @throws PinException PinException
     */
    public PinDescription[] getInputNames() throws PinException {
        ArrayList<PinDescription> pinList = new ArrayList<>();
        for (VisualElement ve : visualElements) {
            if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Clock.DESCRIPTION)) {
                String name = ve.getElementAttributes().getLabel();
                if (name == null || name.length() == 0)
                    throw new PinException(Lang.get("err_pinWithoutName"));

                String descr = ve.getElementAttributes().get(Keys.DESCRIPTION);
                pinList.add(input(name, descr));
            }
        }
        return pinList.toArray(new PinDescription[pinList.size()]);
    }

    /**
     * Returns a list of all output ObservableNames.
     * The ObservableValue is not connected to a model! Its just a wrapper for the outputs name.
     * This method is used to create dummy outputs for a nested element.
     * They are not used, because during creation of a model all inputs and outputs which connect a model
     * with a nested model are removed from the model and replaced by a direct connection of the input or output
     * and the wires of the containing model.
     *
     * @return the list of output ObservableValues
     * @throws PinException PinException
     */
    public ObservableValues getOutputNames() throws PinException {
        ArrayList<ObservableValue> pinList = new ArrayList<>();
        for (VisualElement ve : visualElements) {
            if (ve.equalsDescription(Out.DESCRIPTION)) {
                String name = ve.getElementAttributes().getLabel();
                if (name == null || name.length() == 0)
                    throw new PinException(Lang.get("err_pinWithoutName"));

                String descr = ve.getElementAttributes().get(Keys.DESCRIPTION);
                pinList.add(new ObservableValue(name, 0) {
                    @Override
                    public long getValue() {
                        throw new RuntimeException("invalid call!");
                    }

                    @Override
                    public ObservableValue addObserverToValue(Observer observer) {
                        throw new RuntimeException("invalid call!");
                    }
                }.setDescription(descr));
            }
        }
        return new ObservableValues(pinList);
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
     * @param measurementOrdering list of names
     */
    public void setMeasurementOrdering(List<String> measurementOrdering) {
        this.measurementOrdering = measurementOrdering;
    }

}
