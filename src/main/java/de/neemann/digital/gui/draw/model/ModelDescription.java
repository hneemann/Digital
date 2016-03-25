package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.gui.draw.elements.*;
import de.neemann.digital.gui.draw.library.CustomElement;
import de.neemann.digital.gui.draw.library.ElementLibrary;
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
    public ModelDescription(Circuit circuit, ElementLibrary library) throws PinException {
        this(circuit, library, false);
    }

    public ModelDescription(Circuit circuit, ElementLibrary library, boolean readAsCustom) throws PinException {
        entries = new ArrayList<>();
        netList = new NetList(circuit.getWires());
        if (readAsCustom)
            ioMap = new HashMap<>();
        else
            ioMap = null;

        for (VisualElement vp : circuit.getElements()) {
            Pins pins = vp.getPins();
            ElementTypeDescription elementType = library.getElementType(vp.getElementName());
            Element element = elementType.createElement(vp.getElementAttributes());
            pins.bindOutputsToOutputPins(element.getOutputs());


            // if handled as nested element, don't put pins in EntryList, but put pin in separate map to connect it with parent!
            boolean isNotAIO = true;
            if (readAsCustom) {
                if (elementType == In.DESCRIPTION || elementType == Out.DESCRIPTION) {
                    String label = vp.getElementAttributes().get(AttributeKey.Label);
                    if (label == null || label.length() == 0)
                        throw new PinException(Lang.get("err_pinWithoutName"));
                    if (pins.size() != 1)
                        throw new PinException(Lang.get("err_N_isNotInputOrOutput", label));

                    ioMap.put(label, pins.get(0));
                    isNotAIO = false;
                }
            }

            if (isNotAIO)
                entries.add(new ModelEntry(element, pins, vp, elementType.getInputNames(vp.getElementAttributes())));

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
     * @param bindWiresToValues
     * @return the model
     * @throws PinException
     * @throws NodeException
     */
    public Model createModel(boolean bindWiresToValues) throws PinException, NodeException {
        for (Net n : netList)
            n.interconnect(bindWiresToValues);

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

    public void highLight(Collection<Node> nodes) {
        HashSet<Node> nodeSet = new HashSet<>();
        if (nodes != null)
            nodeSet.addAll(nodes);
        for (ModelEntry me : entries) {
            Element element = me.getElement();
            boolean highLight = element instanceof Node && nodeSet.contains(element);
            me.getVisualElement().setHighLight(highLight);
        }
    }

    public void highLight(VisualElement visualElement) {
        if (visualElement != null) {
            visualElement.setHighLight(true);
        }
    }

    public void highLight(ObservableValue[] values) {
        for (ObservableValue v : values)
            highLight(v);
    }

    private void highLight(ObservableValue v) {
        for (ModelEntry me : entries)
            if (me.containsValue(v))
                me.getVisualElement().setHighLight(true);
        for (Net n : netList)
            if (n.containsValue(v))
                n.setHighLight(true);
    }

    public void highLightOff() {
        for (ModelEntry me : entries)
            me.getVisualElement().setHighLight(false);
        for (Net net : netList)
            net.setHighLight(false);
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
