/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.neemann.digital.XStreamValid;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.*;
import de.neemann.digital.core.arithmetic.BarrelShifterMode;
import de.neemann.digital.core.arithmetic.LeftRightFormat;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.extern.Application;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.DataFieldConverter;
import de.neemann.digital.core.memory.rom.ROMManger;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.shapes.CustomCircuitShapeType;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.gui.components.TransformHolder;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.undo.Copyable;
import de.neemann.gui.language.Language;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * This class contains all the visual elements which form the visual representation of a circuit.
 * Such a Circuit instance is used by the {@link de.neemann.digital.draw.model.ModelCreator} to
 * create a runnable model representation (see {@link de.neemann.digital.core.Model}).
 * This class is also serialized to store a circuit on disk.
 */
public class Circuit implements Copyable<Circuit> {
    private static final Set<Drawable> EMPTY_SET = Collections.emptySet();

    private int version = 1;
    private ElementAttributes attributes;
    private final ArrayList<VisualElement> visualElements;
    private ArrayList<Wire> wires;
    private List<String> measurementOrdering;
    private transient boolean dotsPresent = false;
    private transient File origin;

    /**
     * Creates a proper configured XStream instance
     *
     * @return the XStream instance
     */
    public static XStream getxStream() {
        XStream xStream = new XStreamValid();
        xStream.alias("attributes", ElementAttributes.class);
        xStream.alias("visualElement", VisualElement.class);
        xStream.alias("wire", Wire.class);
        xStream.alias("circuit", Circuit.class);
        xStream.alias("intFormat", IntFormat.class);
        xStream.alias("barrelShifterMode", BarrelShifterMode.class);
        xStream.alias("direction", LeftRightFormat.class);
        xStream.alias("rotation", Rotation.class);
        xStream.aliasAttribute(Rotation.class, "rotation", "rotation");
        xStream.alias("language", Language.class);
        xStream.aliasAttribute(Language.class, "name", "name");
        xStream.alias("vector", Vector.class);
        xStream.aliasAttribute(Vector.class, "x", "x");
        xStream.aliasAttribute(Vector.class, "y", "y");
        xStream.alias("value", InValue.class);
        xStream.aliasAttribute(InValue.class, "value", "v");
        xStream.aliasAttribute(InValue.class, "highZ", "z");
        xStream.addImplicitCollection(ElementAttributes.class, "attributes");
        xStream.alias("data", DataField.class);
        xStream.registerConverter(new DataFieldConverter());
        xStream.alias("testData", TestCaseDescription.class);
        xStream.alias("inverterConfig", InverterConfig.class);
        xStream.addImplicitCollection(InverterConfig.class, "inputs");
        xStream.alias("storedRoms", ROMManger.class);
        xStream.addImplicitCollection(ROMManger.class, "roms");
        xStream.alias("appType", Application.Type.class);
        xStream.ignoreUnknownElements();
        xStream.alias("shape", CustomShapeDescription.class);
        xStream.alias("pin", CustomShapeDescription.Pin.class);
        xStream.alias("circle", CustomShapeDescription.CircleHolder.class);
        xStream.alias("line", CustomShapeDescription.LineHolder.class);
        xStream.alias("poly", CustomShapeDescription.PolygonHolder.class);
        xStream.alias("text", CustomShapeDescription.TextHolder.class);
        xStream.alias("polygon", Polygon.class);
        xStream.alias("shapeType", CustomCircuitShapeType.class);
        xStream.alias("transform", TransformHolder.class);
        xStream.registerConverter(new PolygonConverter());
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
        final Circuit circuit = loadCircuit(new FileInputStream(filename), shapeFactory);
        circuit.origin = filename;
        return circuit;
    }

    /**
     * Creates a new circuit instance from a stored file
     *
     * @param in           the input stream
     * @param shapeFactory shapeFactory used to create the shapes
     * @return the circuit
     * @throws IOException IOException
     */
    public static Circuit loadCircuit(InputStream in, ShapeFactory shapeFactory) throws IOException {
        try {
            XStream xStream = getxStream();
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
        } catch (RuntimeException e) {
            throw new IOException(Lang.get("err_invalidFileFormat"), e);
        } finally {
            in.close();
        }
    }

    /**
     * Stores the circuit in the given file
     *
     * @param filename filename
     * @throws IOException IOException
     */
    public void save(File filename) throws IOException {
        save(new FileOutputStream(filename));
    }

    /**
     * Stores the circuit in the given file
     *
     * @param out the writer
     * @throws IOException IOException
     */
    public void save(OutputStream out) throws IOException {
        try (Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            XStream xStream = Circuit.getxStream();
            w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(this, new PrettyPrintWriter(w));
        }
    }

    /**
     * Creates a new empty circuit instance
     */
    public Circuit() {
        visualElements = new ArrayList<>();
        wires = new ArrayList<>();
    }

    /**
     * Creates a copy of the given circuit
     *
     * @param original the original
     */
    private Circuit(Circuit original) {
        this();
        for (VisualElement ve : original.visualElements)
            visualElements.add(new VisualElement(ve));
        for (Wire w : original.wires)
            wires.add(new Wire(w));
        if (original.attributes != null)
            attributes = new ElementAttributes(original.attributes);

        measurementOrdering = new ArrayList<>();
        if (original.measurementOrdering != null)
            measurementOrdering.addAll(original.measurementOrdering);

        origin = original.origin;

        version = 1;
    }

    @Override
    public Circuit createDeepCopy() {
        return new Circuit(this);
    }

    /**
     * Create a shallow copy of this circuit
     *
     * @return the shallow copy
     */
    public Circuit createShallowCopy() {
        Circuit circuit = new Circuit();
        if (attributes != null)
            circuit.attributes = new ElementAttributes(attributes);
        circuit.wires.addAll(wires);
        circuit.visualElements.addAll(visualElements);
        circuit.origin = origin;
        return circuit;
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
     * Draws tis circuit using the given graphic instance
     *
     * @param graphic the graphic instance used
     */
    public void drawTo(Graphic graphic) {
        drawTo(graphic, EMPTY_SET, null, SyncAccess.NOSYNC);
    }

    /**
     * Draws this circuit using the given graphic instance
     *
     * @param graphic     the graphic instance used
     * @param highLighted a list of Drawables to highlight
     * @param highlight   style used to draw the highlighted elements
     * @param modelSync   sync interface to access the model. Is locked while drawing circuit
     */
    public void drawTo(Graphic graphic, Collection<Drawable> highLighted, Style highlight, SyncAccess modelSync) {
        if (!dotsPresent) {
            new DotCreator(wires).applyDots();
            dotsPresent = true;
        }

        // reads the models state which is a fast operation
        modelSync.read(() -> {
            for (Wire w : wires)
                w.readObservableValues();
            for (VisualElement p : visualElements)
                p.getShape().readObservableValues();
        });

        // after that draw the model which is rather slow
        graphic.openGroup();
        for (Wire w : wires)
            w.drawTo(graphic, highLighted.contains(w) ? highlight : null);
        graphic.closeGroup();
        for (VisualElement p : visualElements) {
            graphic.openGroup();
            p.drawTo(graphic, highLighted.contains(p) ? highlight : null);
            graphic.closeGroup();
        }
    }

    /**
     * Adds a ne VisualElement
     *
     * @param visualElement the visual element to add
     * @return this for chained calls
     */
    public Circuit add(VisualElement visualElement) {
        visualElements.add(visualElement);
        return this;
    }

    /**
     * Adds a new Wire
     *
     * @param newWire the wire to add
     * @return this for chained calls
     */
    public Circuit add(Wire newWire) {
        if (newWire.p1.equals(newWire.p2))
            return null;

        wires.add(newWire);
        WireConsistencyChecker checker = new WireConsistencyChecker(wires);
        wires = checker.check();

        dotsPresent = false;
        return this;
    }

    /**
     * Called if elements are moved
     */
    public void elementsMoved() {
        WireConsistencyChecker checker = new WireConsistencyChecker(wires);
        wires = checker.check();

        dotsPresent = false;
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
     * Find specific visual elements
     *
     * @param filter the filter
     * @return the elements
     */
    public List<VisualElement> getElements(Circuit.ElementFilter filter) {
        ArrayList<VisualElement> found = new ArrayList<>();
        for (VisualElement v : visualElements)
            if (filter.accept(v))
                found.add(v);
        return found;
    }

    /**
     * Returns all enabled test cases in the circuit
     *
     * @return the test case elements
     */
    public List<VisualElement> getTestCases() {
        return getElements(v -> v.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION) && v.getElementAttributes().get(Keys.ENABLED));
    }

    /**
     * Returns a list of all Moveables in the given rectangle.
     *
     * @param min upper left corner of the rectangle
     * @param max lower right corner of the rectangle
     * @return the list
     */
    public ArrayList<Movable> getElementsToMove(Vector min, Vector max) {
        ArrayList<Movable> m = new ArrayList<>();
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
     * @param min          upper left corner of the rectangle
     * @param max          lower right corner of the rectangle
     * @param shapeFactory the shape factory
     * @return the list
     */
    public ArrayList<Movable> copyElementsToMove(Vector min, Vector max, ShapeFactory shapeFactory) {
        ArrayList<Movable> m = new ArrayList<>();
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
     * Returns a list of all Drawables and Moveables in the given rectangle.
     * It creates a deep copy of all elements.
     *
     * @param min          upper left corner of the rectangle
     * @param max          lower right corner of the rectangle
     * @param shapeFactory the shape factory
     * @return the list
     */
    public RectContainer copyElementsInRect(Vector min, Vector max, ShapeFactory shapeFactory) {
        ArrayList<Drawable> d = new ArrayList<>();
        ArrayList<Movable> m = new ArrayList<>();
        for (VisualElement vp : visualElements)
            if (vp.matches(min, max)) {
                final VisualElement ve = new VisualElement(vp).setShapeFactory(shapeFactory);
                m.add(ve);
                d.add(ve);
            }

        for (Wire w : wires) {
            final boolean p1Inside = w.p1.inside(min, max);
            final boolean p2Inside = w.p2.inside(min, max);
            if (p1Inside || p2Inside) {
                final Wire ww = new Wire(w);
                d.add(ww);
                if (p1Inside)
                    m.add(ww.getMovableP1());
                if (p2Inside)
                    m.add(ww.getMovableP2());
            }
        }

        if (m.isEmpty() && d.isEmpty())
            return null;
        else
            return new RectContainer(d, m);
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
    }

    /**
     * Deletes a single visual element
     *
     * @param partToDelete the element to delete
     */
    public void delete(VisualElement partToDelete) {
        visualElements.remove(partToDelete);
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
        }
    }


    /**
     * Returns the element at the given position
     *
     * @param pos the cursor position
     * @return the first element or null if there is no element at the given position
     */
    public VisualElement getElementAt(Vector pos) {
        return getElementAt(pos, false);
    }

    /**
     * Returns the element at the given position
     *
     * @param pos         the cursor position
     * @param includeText if true the label text is included in matching
     * @return the first element or null if there is no element at the given position
     */
    public VisualElement getElementAt(Vector pos, boolean includeText) {
        for (VisualElement element : visualElements)
            if (element.matches(pos, includeText))
                return element;
        return null;
    }

    /**
     * Returns a list of elements at the given position.
     *
     * @param pos         the cursor position
     * @param includeText if true the element is also returned if only the text matches the given position
     * @return the elements or an empty list if there is no element at the given position
     */
    public List<VisualElement> getElementListAt(Vector pos, boolean includeText) {
        ArrayList<VisualElement> list = new ArrayList<>();
        for (VisualElement element : visualElements)
            if (element.matches(pos, includeText))
                list.add(element);
        return list;
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

        return el.isPinPos(pos);
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
     * @param pos    the position
     * @param radius the catching distance
     * @return the matching wire or null
     */
    public Wire getWireAt(Vector pos, int radius) {
        float minDist = 0;
        Wire best = null;
        for (Wire w : wires)
            if (w.contains(pos, radius)) {
                float d = w.distance(pos);
                if (best == null || d < minDist) {
                    minDist = d;
                    best = w;
                }
            }
        return best;
    }

    /**
     * Deletes the references to the ObservableValues representing the elements or wire state.
     * So this circuit is detached from a generated model.
     */
    public void clearState() {
        for (VisualElement vp : visualElements)
            vp.setState(null);
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
                final ElementAttributes attr = ve.getElementAttributes();
                String name = attr.getLabel();
                if (name == null || name.length() == 0) {
                    if (ve.equalsDescription(Clock.DESCRIPTION))
                        throw new PinException(Lang.get("err_clockWithoutName"));
                    else
                        throw new PinException(Lang.get("err_pinWithoutName"));
                }

                PinInfo pin;
                if (ve.equalsDescription(Clock.DESCRIPTION))
                    pin = input(name, Lang.get("elem_Clock")).setClock();
                else
                    pin = input(name, Lang.evalMultilingualContent(attr.get(Keys.DESCRIPTION)));
                pinList.add(pin.setPinNumber(attr.get(Keys.PINNUMBER)));
            }
        }
        return pinList.toArray(new PinDescription[0]);
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
                final ElementAttributes attr = ve.getElementAttributes();
                String name = attr.getLabel();
                if (name == null || name.length() == 0)
                    throw new PinException(Lang.get("err_pinWithoutName"));

                String descr = Lang.evalMultilingualContent(attr.get(Keys.DESCRIPTION));
                pinList.add(new ObservableValue(name, 0) {
                    @Override
                    public long getValue() {
                        throw new RuntimeException("invalid call!");
                    }

                    @Override
                    public ObservableValue addObserverToValue(Observer observer) {
                        throw new RuntimeException("invalid call!");
                    }

                    @Override
                    public int getBits() {
                        throw new RuntimeException("invalid call!");
                    }
                }.setDescription(descr).setPinNumber(attr.get(Keys.PINNUMBER)));
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

    /**
     * @return the file origin of this circuit, may be null
     */
    public File getOrigin() {
        return origin;
    }

    /**
     * Sets the file name
     *
     * @param filename the file name
     */
    public void setOrigin(File filename) {
        this.origin = filename;
    }

    /**
     * Visual element filter
     */
    public interface ElementFilter {
        /**
         * Accepts a specific visible element
         *
         * @param v the element
         * @return true if accepted
         */
        boolean accept(VisualElement v);
    }

    /**
     * Container to return movables and drawables at once.
     */
    public static final class RectContainer {
        private final ArrayList<Drawable> d;
        private final ArrayList<Movable> m;

        private RectContainer(ArrayList<Drawable> d, ArrayList<Movable> m) {
            this.d = d;
            this.m = m;
        }

        /**
         * @return the list of drawables
         */
        public ArrayList<Drawable> getDrawables() {
            return d;
        }

        /**
         * @return the list of movables
         */
        public ArrayList<Movable> getMovables() {
            return m;
        }
    }
}
