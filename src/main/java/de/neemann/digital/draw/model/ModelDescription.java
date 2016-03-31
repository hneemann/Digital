package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * @author hneemann
 */
public class ModelDescription implements Iterable<ModelEntry> {

    private final NetList netList;
    private final ArrayList<ModelEntry> entries;
    private final HashMap<String, Pin> ioMap;
    private HashMap<Node, ModelEntry> map;

    /**
     * Creates the ModelDescription.
     * In created the NetList is complete, so all pins connected together are registered
     * to the Net instances in the NetList. Every group of connected Pins is represented
     * by a Net instance in the NetList.
     *
     * @param circuit the circuit
     * @param library the library used to create the Element instances
     * @throws PinException
     */
    public ModelDescription(Circuit circuit, ElementLibrary library) throws PinException, NodeException {
        this(circuit, library, false);
    }
    public ModelDescription(Circuit circuit, ElementLibrary library, boolean readAsCustom) throws PinException, NodeException {
        this(circuit, library, readAsCustom, "unknown", new NetList(circuit.getWires()));
    }

    public ModelDescription(Circuit circuit, ElementLibrary library, boolean readAsCustom, String fileName, NetList netList) throws PinException, NodeException {
        this.netList = netList;
        entries = new ArrayList<>();
        if (readAsCustom)
            ioMap = new HashMap<>();
        else
            ioMap = null;

        for (VisualElement ve : circuit.getElements()) {
            Pins pins = ve.getPins();
            ElementTypeDescription elementType = library.getElementType(ve.getElementName());
            Element element = elementType.createElement(ve.getElementAttributes());
            ve.setElement(element);
            pins.bindOutputsToOutputPins(element.getOutputs());


            // if handled as nested element, don't put pins in EntryList, but put pin in separate map to connect it with parent!
            boolean isNotAIO = true;
            if (readAsCustom) {
                if (elementType == In.DESCRIPTION || elementType == Out.DESCRIPTION) {
                    String label = ve.getElementAttributes().get(AttributeKey.Label);
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
                entries.add(new ModelEntry(element, pins, ve, elementType.getInputNames(ve.getElementAttributes())));

            for (Pin p : pins)
                netList.add(p);
        }


        ArrayList<ModelDescription> cmdl = new ArrayList<>();
        Iterator<ModelEntry> it = entries.iterator();
        while (it.hasNext()) {
            ModelEntry me = it.next();
            if (me.getElement() instanceof CustomElement) {
                CustomElement ce = (CustomElement) me.getElement();
                ModelDescription child = ce.getModelDescription();
                cmdl.add(child);

                for (Pin p : me.getPins()) {
                    Net childNet = child.getNetOfIOandRemove(p.getName());
                    Net thisNet = netList.getNetOfPin(p);
                    if (thisNet != null) {
                        // Disconnect the parents net from the pin
                        thisNet.removePin(p);
                        // and connect it to the nested inner net!
                        thisNet.addAll(childNet.getPins());
                        // remove connected net form child
                        child.remove(childNet);
                    }
                }
                it.remove();
            }
        }
        for (ModelDescription md : cmdl) {
            entries.addAll(md.entries);
            netList.add(md.netList);
        }
    }

    private void remove(Net childNet) {
        netList.remove(childNet);
    }

    private Net getNetOfIOandRemove(String name) throws PinException {
        Pin pin = ioMap.get(name);
        if (pin == null)
            throw new PinException(Lang.get("err_pin_N_notFound", name));
        Net netOfPin = netList.getNetOfPin(pin);
        if (netOfPin == null)
            throw new PinException(Lang.get("err_netOfPin_N_notFound", name));

        netOfPin.removePin(pin);

        return netOfPin;
    }

    /**
     * Creates the model
     *
     * @return the model
     * @throws PinException
     * @throws NodeException
     */
    public Model createModel() throws PinException, NodeException {
        for (Net n : netList)
            n.interconnect();

        Model m = new Model();

        for (ModelEntry e : entries)
            e.applyInputs();

        for (ModelEntry e : entries)
            e.getElement().registerNodes(m);

        for (ModelEntry e : entries)
            e.getElement().init();

        return m;
    }

    /**
     * Needs to be called after create model is called!
     * Connects the gui to the model
     *
     * @param guiObserver
     */
    public void connectToGui(Observer guiObserver) {
        for (ModelEntry e : entries)
            e.connectToGui(guiObserver);

    }

    public void addNodeElementsTo(Collection<Node> nodes, Collection<Drawable> highLighted) {
        HashSet<Node> nodeSet = new HashSet<>();
        if (nodes != null)
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

    public List<ModelEntry> getEntries(String elementName) {
        List<ModelEntry> entr = new ArrayList<>();
        for (ModelEntry me : entries)
            if (me.getVisualElement().getElementName().endsWith(elementName))
                entr.add(me);
        return entr;
    }

}
