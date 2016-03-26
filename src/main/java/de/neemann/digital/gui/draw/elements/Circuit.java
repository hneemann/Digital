package de.neemann.digital.gui.draw.elements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.library.ElementLibrary;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class Circuit implements Drawable {
    private final ArrayList<VisualElement> visualElements;
    private ArrayList<Wire> wires;
    private transient boolean dotsPresent = false;
    private transient boolean modified = false;


    private static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("visualElement", VisualElement.class);
        xStream.alias("wire", Wire.class);
        xStream.alias("circuit", Circuit.class);
        xStream.alias("vector", Vector.class);
        xStream.aliasAttribute(Vector.class, "x", "x");
        xStream.aliasAttribute(Vector.class, "y", "y");
        //xStream.alias("key", AttributeKey.class);
        xStream.addImplicitCollection(ElementAttributes.class, "attributes");
        return xStream;
    }

    public static Circuit loadCircuit(File filename) throws IOException {
        XStream xStream = getxStream();
        try (InputStream in = new FileInputStream(filename)) {
            return (Circuit) xStream.fromXML(in);
        }
    }

    public void save(File filename) throws IOException {
        XStream xStream = Circuit.getxStream();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(filename), "utf-8")) {
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(this, new PrettyPrintWriter(out));
            modified = false;
        }
    }

    public Circuit() {
        visualElements = new ArrayList<>();
        wires = new ArrayList<>();
    }

    @Override
    public void drawTo(Graphic graphic) {
        if (!dotsPresent) {
            new DotCreator(wires).applyDots();
            dotsPresent = true;
        }

        for (Wire w : wires)
            w.drawTo(graphic);
        for (VisualElement p : visualElements)
            p.drawTo(graphic);
    }

    public void add(VisualElement visualElement) {
        visualElements.add(visualElement);
        modified();
    }

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

    public ArrayList<VisualElement> getElements() {
        return visualElements;
    }

    public ArrayList<Moveable> getElementsToMove(Vector min, Vector max) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualElement vp : visualElements)
            if (vp.matches(min, max))
                m.add(vp);

        for (Wire w : wires) {
            if (w.p1.inside(min, max))
                m.add(w.p1);
            if (w.p2.inside(min, max))
                m.add(w.p2);
        }
        if (m.isEmpty())
            return null;
        else
            return m;
    }

    public ArrayList<Moveable> getElementsToCopy(Vector min, Vector max) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualElement vp : visualElements)
            if (vp.matches(min, max))
                m.add(new VisualElement(vp));

        for (Wire w : wires)
            if (w.p1.inside(min, max) && w.p2.inside(min, max))
                m.add(new Wire(w));

        if (m.isEmpty())
            return null;
        else
            return m;
    }


    public void delete(Vector min, Vector max) {
        {
            Iterator<VisualElement> it = visualElements.iterator();
            while (it.hasNext())
                if (it.next().matches(min, max))
                    it.remove();
        }
        {
            Iterator<Wire> it = wires.iterator();
            while (it.hasNext()) {
                Wire w = it.next();
                if (w.p1.inside(min, max) || w.p2.inside(min, max))
                    it.remove();
            }
        }
        dotsPresent = false;
        modified();
    }

    public VisualElement getElementAt(Vector pos) {
        for (VisualElement element : visualElements) {
            if (element.matches(pos))
                return element;
        }
        return null;
    }

    public void modified() {
        modified = true;
    }

    public ArrayList<Wire> getWires() {
        return wires;
    }

    public void clearState() {
        for (VisualElement vp : visualElements)
            vp.setState(null, null);
        for (Wire w : wires)
            w.setValue(null);
    }

    public boolean isModified() {
        return modified;
    }

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
                        throw new RuntimeException("invallid call!");
                    }

                    @Override
                    public ObservableValue addObserver(Observer observer) {
                        throw new RuntimeException("invallid call!");
                    }
                });
            }
        }
        return pinList.toArray(new ObservableValue[pinList.size()]);
    }

}
