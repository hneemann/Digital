package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartTypeDescription;
import de.neemann.digital.gui.draw.library.PartLibrary;
import de.neemann.digital.gui.draw.parts.*;

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
        for (VisualPart vp : circuit.getParts()) {
            Pins pins = vp.getPins();
            PartTypeDescription partType = library.getPartType(vp.getPartName());
            Part part = partType.createPart(vp.getPartAttributes());
            pins.setOutputs(part.getOutputs());

            entries.add(new ModelEntry(part, pins, vp, partType.getInputNames(vp.getPartAttributes())));
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
            e.getPart().registerNodes(m);

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
            Part part = me.getPart();
            boolean highLight = part instanceof Node && nodeSet.contains(part);
            me.getVisualPart().setHighLight(highLight);
        }
    }

}
