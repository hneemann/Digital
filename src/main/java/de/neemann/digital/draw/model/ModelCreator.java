/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.rom.ROMManger;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.LibraryInterface;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.lang.Lang;

import java.util.*;


/**
 * Creates a {@link Model} from the given {@link Circuit} instance.
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
    public ModelCreator(Circuit circuit, LibraryInterface library) throws PinException, NodeException, ElementNotFoundException {
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
    public ModelCreator(Circuit circuit, LibraryInterface library, boolean readAsCustom) throws PinException, NodeException, ElementNotFoundException {
        this(circuit, library, readAsCustom, new NetList(circuit), "", 0, null);
    }

    /**
     * Creates a new instance
     *
     * @param circuit                 the circuit to use
     * @param library                 the library to use
     * @param isNestedCircuit         if true the model is created for use as nested element
     * @param netList                 the NetList of the model. If known it is not necessary to create it.
     * @param subName                 name of the circuit, used to name unique elements
     * @param depth                   recursion depth, used to detect a circuit which contains itself
     * @param containingVisualElement the topmost containing visual element
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator(Circuit circuit, LibraryInterface library, boolean isNestedCircuit, NetList netList, String subName, int depth, VisualElement containingVisualElement) throws PinException, NodeException, ElementNotFoundException {
        this.circuit = circuit;
        this.netList = netList;
        entries = new ArrayList<>();
        if (isNestedCircuit)
            ioMap = new HashMap<>();
        else
            ioMap = null;

        if (!isNestedCircuit)
            checkWiresForSplitterConnection(circuit);

        try {
            for (VisualElement ve : circuit.getElements()) {
                VisualElement cve = ve;
                if (containingVisualElement != null)
                    cve = containingVisualElement;

                ElementAttributes attr = ve.getElementAttributes();
                ElementTypeDescription elementType = library.getElementType(ve.getElementName(), attr);
                if (attr.getLabel().contains("*")
                        && !ve.equalsDescription(In.DESCRIPTION)
                        && !ve.equalsDescription(Out.DESCRIPTION)) {
                    attr = new ElementAttributes(attr);
                    attr.set(Keys.LABEL, attr.getLabel().replace("*", subName));
                }
                Element element = elementType.createElement(attr);
                ve.setElement(element);
                Pins pins = ve.getPins();
                pins.bindOutputsToOutputPins(element.getOutputs());

                // sets the nodes origin to create better error messages
                if (element instanceof Node)
                    ((Node) element).setOrigin(circuit.getOrigin());

                // if handled as nested element, don't put pins in EntryList, but put the pins in a
                // separate map to connect them with the parent!
                boolean isNotAIO = true;
                if (isNestedCircuit) {
                    if (elementType == In.DESCRIPTION || elementType == Out.DESCRIPTION || elementType == Clock.DESCRIPTION) {
                        String label = ve.getElementAttributes().getLabel();
                        if (label == null || label.length() == 0)
                            throw new PinException(Lang.get("err_pinWithoutName", circuit.getOrigin()), cve);
                        if (pins.size() != 1)
                            throw new PinException(Lang.get("err_N_isNotInputOrOutput", label, circuit.getOrigin()), cve);
                        if (ioMap.containsKey(label))
                            throw new PinException(Lang.get("err_duplicatePinLabel", label, circuit.getOrigin()), cve);

                        ioMap.put(label, pins.get(0));
                        isNotAIO = false;
                    }
                }

                if (isNotAIO)
                    entries.add(new ModelEntry(element, pins, ve, elementType.getInputDescription(ve.getElementAttributes()), isNestedCircuit, circuit.getOrigin(), cve));

                for (Pin p : pins)
                    netList.add(p);
            }

            // connect all custom elements to the parents net
            ArrayList<ModelCreator> modelCreators = new ArrayList<>();
            Iterator<ModelEntry> it = entries.iterator();
            while (it.hasNext()) {
                ModelEntry me = it.next();
                if (me.getElement() instanceof CustomElement) {        // at first look for custom elements

                    CustomElement ce = (CustomElement) me.getElement();
                    ModelCreator child = ce.getModelCreator(
                            combineNames(subName, me.getVisualElement().getElementAttributes().getLabel()),
                            depth + 1,
                            containingVisualElement != null ? containingVisualElement : me.getVisualElement(),
                            me.getVisualElement(), library);
                    modelCreators.add(child);

                    HashMap<Net, Net> netMatch = new HashMap<>();

                    for (Pin p : me.getPins()) {                     // connect the custom elements to the parents net
                        Net childNet = child.getNetOfIOAndRemove(p);
                        if (childNet != null) {
                            Net otherParentNet = netMatch.get(childNet);
                            if (otherParentNet != null) {
                                // direct connection!
                                // two nets in the parent are connected directly by the nested circuit
                                // merge the nets in the parent!

                                // remove the children's inner pin which is already added to the other net
                                Pin insertedPin = child.getPinOfIO(p.getName());
                                otherParentNet.removePin(insertedPin);

                                Net parentNet = netList.getNetOfPin(p);
                                if (parentNet != null) {
                                    // Disconnect the parents net from the pin
                                    parentNet.removePin(p);

                                    // connect the two parent nets if they are not already the same
                                    if (otherParentNet != parentNet) {
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
                    }

                    // remove connected nets form child
                    for (Net childNet : netMatch.keySet())
                        child.remove(childNet);

                    it.remove();
                }
            }
            for (ModelCreator md : modelCreators) {       // put the elements of the custom element to the parent
                entries.addAll(md.entries);
                netList.add(md.netList);
            }
        } catch (PinException | NodeException e) {
            e.setOrigin(circuit.getOrigin());
            e.setVisualElement(containingVisualElement);
            throw e;
        }
    }

    private void checkWiresForSplitterConnection(Circuit circuit) {
        HashSet<Vector> posSet = new HashSet<>();
        for (VisualElement e : circuit.getElements())
            if (e.equalsDescription(Splitter.DESCRIPTION))
                for (Pin p : e.getPins())
                    posSet.add(p.getPos());
        for (Wire w : circuit.getWires())
            w.setIsConnectedToSplitter(posSet.contains(w.p1) || posSet.contains(w.p2));
    }

    private String combineNames(String s1, String s2) {
        if (s1.length() > 0) {
            if (s2.length() > 0) {
                return s1 + "-" + s2;
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

    private Net getNetOfIOAndRemove(Pin p) throws PinException {
        Pin pin = getPinOfIO(p.getName());
        Net netOfPin = netList.getNetOfPin(pin);

        if (netOfPin == null) {
            if (p.getDirection() == PinDescription.Direction.input)
                return null;
            else
                throw new PinException(Lang.get("err_netOfPin_N_notFound", p.getName()));
        }

        netOfPin.removePin(pin);

        return netOfPin;
    }

    /**
     * Creates the model.
     *
     * @param attachWires if true the wires are attached to the values
     * @return the model
     * @throws PinException  PinException
     * @throws NodeException NodeException
     */
    public Model createModel(boolean attachWires) throws PinException, NodeException {
        Model m = new Model().setAllowGlobalValues(attachWires);

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

        ROMManger romManager = circuit.getAttributes().get(Keys.ROMMANAGER);
        romManager.applyTo(m);

        return m;
    }

    /**
     * Needs to be called after createModel is called!
     * Connects the gui to the model
     */
    public void connectToGui() {
        for (ModelEntry e : entries)
            e.connectToGui();
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

        HashSet<Node> nodeSet = new HashSet<>(nodes);
        for (ModelEntry me : entries) {
            Element element = me.getElement();
            if (element instanceof Node && nodeSet.contains(element))
                highLighted.add(me.getContainingVisualElement());
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
        List<ModelEntry> entry = new ArrayList<>();
        for (ModelEntry me : entries)
            if (me.getVisualElement().getElementName().endsWith(elementName))
                entry.add(me);
        return entry;
    }

    /**
     * @return the circuit which was used to create this model description
     */
    public Circuit getCircuit() {
        return circuit;
    }
}
