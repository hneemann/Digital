package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.gui.draw.elements.*;
import de.neemann.digital.gui.draw.library.PartLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author hneemann
 */
public class ModelDescription {

    private final Circuit circuit;
    private final NetList netList;
    private final ArrayList<ModelEntry> entries;
    private HashMap<Node, ModelEntry> map;

    public ModelDescription(Circuit circuit, PartLibrary library) throws PinException {
        this.circuit = circuit;
        entries = new ArrayList<>();
        netList = new NetList(circuit.getWires());
        for (VisualElement vp : circuit.getParts()) {
            Pins pins = vp.getPins();
            ElementTypeDescription partType = library.getPartType(vp.getElementName());
            Element element = partType.createPart(vp.getElementAttributes());
            pins.setOutputs(element.getOutputs());

            entries.add(new ModelEntry(element, pins, vp, partType.getInputNames(vp.getElementAttributes())));
            for (Pin p : pins)
                netList.add(p);
        }
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
}
