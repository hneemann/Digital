package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.util.*;

/**
 * Creates a {@link Model} from the given {@link Circuit} instance.
 *
 * @author hneemann
 */
public class ModelCreator implements Iterable<ModelEntry> {

    private final Circuit circuit;
    private final NetList netList;
    private final ArrayList<ModelEntry> entries;
    private final HashMap<String, Pin> ioMap;

    /**
     * Creates the ModelDescription.
     * If created, the NetList is complete. So all pins which are connected together are registered
     * to the Net instances in the NetList. Every group of connected pins is represented
     * by a Net instance in the NetList.
     *
     * @param circuit the circuit
     * @param library the library used to create the Element instances
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator(Circuit circuit, ElementLibrary library) throws PinException, NodeException, ElementNotFoundException {
        this(circuit, library, false);
    }

    /**
     * Creates a new instance
     *
     * @param circuit      the circuit to use
     * @param library      the library to use
     * @param readAsCustom if true the model is created for use as nested element
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator(Circuit circuit, ElementLibrary library, boolean readAsCustom) throws PinException, NodeException, ElementNotFoundException {
        this(circuit, library, readAsCustom, null, new NetList(circuit), "");
    }

    /**
     * Creates a new instance
     *
     * @param circuit         the circuit to use
     * @param library         the library to use
     * @param isNestedCircuit if true the model is created for use as nested element
     * @param fileName        only used for better messages in exceptions
     * @param netList         the NetList of the model. If known it is not necessary to create it.
     * @param subName         name of the circuit, used to name unique elements
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator(Circuit circuit, ElementLibrary library, boolean isNestedCircuit, File fileName, NetList netList, String subName) throws PinException, NodeException, ElementNotFoundException {
        this.circuit = circuit;
        this.netList = netList;
        entries = new ArrayList<>();
        if (isNestedCircuit)
            ioMap = new HashMap<>();
        else
            ioMap = null;

        for (VisualElement ve : circuit.getElements()) {
            Pins pins = ve.getPins();
            ElementTypeDescription elementType = library.getElementType(ve.getElementName());
            ElementAttributes attr = ve.getElementAttributes();
            if (attr.getCleanLabel().contains("*")) {
                attr = new ElementAttributes(attr);
                attr.set(Keys.LABEL, attr.getCleanLabel().replace("*", subName));
            }
            Element element = elementType.createElement(attr);
            ve.setElement(element);
            pins.bindOutputsToOutputPins(element.getOutputs());

            // sets the nodes origin to create better error messages
            if (element instanceof Node)
                ((Node) element).setOrigin(fileName);

            // if handled as nested element, don't put pins in EntryList, but put the pins in a
            // separate map to connect it with the parent!
            boolean isNotAIO = true;
            if (isNestedCircuit) {
                if (elementType == In.DESCRIPTION || elementType == Out.DESCRIPTION || elementType == Clock.DESCRIPTION) {
                    String label = ve.getElementAttributes().getLabel();
                    if (label == null || label.length() == 0)
                        throw new PinException(Lang.get("err_pinWithoutName", fileName));
                    if (pins.size() != 1)
                        throw new PinException(Lang.get("err_N_isNotInputOrOutput", label, fileName));
                    if (ioMap.containsKey(label))
                        throw new PinException(Lang.get("err_duplicatePinLabel", label, fileName));

                    ioMap.put(label, pins.get(0));
                    isNotAIO = false;
                }
            }

            if (isNotAIO)
                entries.add(new ModelEntry(element, pins, ve, elementType.getInputDescription(ve.getElementAttributes()), isNestedCircuit));

            for (Pin p : pins)
                netList.add(p);
        }

        // connect all custom elements to the parents net
        ArrayList<ModelCreator> cmdl = new ArrayList<>();
        Iterator<ModelEntry> it = entries.iterator();
        while (it.hasNext()) {
            ModelEntry me = it.next();
            if (me.getElement() instanceof CustomElement) {        // at first look for custom elements
                CustomElement ce = (CustomElement) me.getElement();
                ModelCreator child = ce.getModelDescription(combineNames(subName, me.getVisualElement().getElementAttributes().getCleanLabel()));
                cmdl.add(child);

                HashMap<Net, Net> netMatch = new HashMap<>();

                for (Pin p : me.getPins()) {                     // connect the custom elements to the parents net
                    Net childNet = child.getNetOfIOandRemove(p.getName());

                    Net otherParentNet = netMatch.get(childNet);
                    if (otherParentNet != null) {
                        // direct connection!
                        // two nets in the parent are connected directly by the nested circuit
                        // merge the nets in the parent!

                        // remove the childs inner pin which is already added to the other net
                        Pin insertedPin = child.getPinOfIO(p.getName());
                        otherParentNet.removePin(insertedPin);

                        Net parentNet = netList.getNetOfPin(p);
                        if (parentNet != null) {
                            // Disconnect the parents net from the pin
                            parentNet.removePin(p);

                            // connect the two parent nets is they are not already the same
                            if (otherParentNet!=parentNet) {
                                otherParentNet.addNet(parentNet);
                                netList.remove(parentNet);
                            }
                        }
                    } else {
                        Net parentNet = netList.getNetOfPin(p);
                        if (parentNet != null) {
                            // Disconnect the parents net from the pin
                            parentNet.removePin(p);
                            // and connect it to the nested inner net!
                            parentNet.addAll(childNet.getPins());

                            // store net connection
                            netMatch.put(childNet, parentNet);
                        }
                    }
                }

                // remove connected nets form child
                for (Net childNet : netMatch.keySet())
                    child.remove(childNet);

                it.remove();
            }
        }
        for (ModelCreator md : cmdl) {       // put the elements of the custom element to the parent
            entries.addAll(md.entries);
            netList.add(md.netList);
        }
    }

    private String combineNames(String s1, String s2) {
        if (s1.length() > 0) {
            if (s2.length() > 0) {
                return s1 + "_" + s2;
            } else
                return s1;
        } else {
            return s2;
        }
    }

    private void remove(Net childNet) {
        netList.remove(childNet);
    }

    private Pin getPinOfIO(String name) throws PinException {
        Pin pin = ioMap.get(name);
        if (pin == null)
            throw new PinException(Lang.get("err_pin_N_notFound", name));
        return pin;
    }

    private Net getNetOfIOandRemove(String name) throws PinException {
        Pin pin = getPinOfIO(name);
        Net netOfPin = netList.getNetOfPin(pin);
        if (netOfPin == null)
            throw new PinException(Lang.get("err_netOfPin_N_notFound", name));

        netOfPin.removePin(pin);

        return netOfPin;
    }

    /**
     * Creates the model
     *
     * @param attachWires if true the wires are attached to the values
     * @return the model
     * @throws PinException  PinException
     * @throws NodeException NodeException
     */
    public Model createModel(boolean attachWires) throws PinException, NodeException {

        Model m = new Model();

        for (Net n : netList)
            n.interconnect(m, attachWires);

        for (ModelEntry e : entries)
            e.applyInputs();

        for (ModelEntry e : entries)
            e.getElement().registerNodes(m);

        for (ModelEntry e : entries) {
            e.getElement().init(m);
            e.getVisualElement().getShape().registerModel(this, m, e);
        }

        return m;
    }

    /**
     * Needs to be called after createModel is called!
     * Connects the gui to the model
     *
     * @param guiObserver the observer which can be attached to {@link de.neemann.digital.core.ObservableValue}s
     *                    which have a state dependant graphical representation.
     */
    public void connectToGui(Observer guiObserver) {
        for (ModelEntry e : entries)
            e.connectToGui(guiObserver);
    }

    /**
     * Adds all the VisualElements, witch have generated one of the given nodes to the collection
     * of Drawables.
     *
     * @param nodes       The collection of nodes
     * @param highLighted the list of drawables to add the VisualElements to
     */
    public void addNodeElementsTo(Collection<Node> nodes, Collection<Drawable> highLighted) {
        if (nodes == null) return;

        HashSet<Node> nodeSet = new HashSet<>();
        nodeSet.addAll(nodes);
        for (ModelEntry me : entries) {
            Element element = me.getElement();
            if (element instanceof Node && nodeSet.contains(element))
                highLighted.add(me.getVisualElement());
        }
    }

    @Override
    public Iterator<ModelEntry> iterator() {
        return entries.iterator();
    }

    /**
     * Returns a list of all ModelEntries which contain an element of the given name
     *
     * @param elementName the name of the element
     * @return the list
     */
    public List<ModelEntry> getEntries(String elementName) {
        List<ModelEntry> entr = new ArrayList<>();
        for (ModelEntry me : entries)
            if (me.getVisualElement().getElementName().endsWith(elementName))
                entr.add(me);
        return entr;
    }

    /**
     * @return the circuit which was used to create this model description
     */
    public Circuit getCircuit() {
        return circuit;
    }
}
