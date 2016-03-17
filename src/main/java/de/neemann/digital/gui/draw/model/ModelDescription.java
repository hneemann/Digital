package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Part;
import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.parts.*;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ModelDescription {

    private final Circuit circuit;
    private final NetList netList;
    private final ArrayList<ModelEntry> entries;

    public ModelDescription(Circuit circuit) throws PinException {
        this.circuit = circuit;
        entries = new ArrayList<>();
        netList = new NetList(circuit.getWires());
        for (VisualPart vp : circuit.getParts()) {
            PartDescription partDescription = vp.getPartDescription();
            Pins pins = vp.getPins();
            Part part = partDescription.create();
            pins.setOutputs(part.getOutputs());

            entries.add(new ModelEntry(part, pins, partDescription.getInputNames()));
            for (Pin p : pins)
                netList.add(p);
        }
    }

    public Model create() throws PinException, NodeException {
        for (Net n : netList)
            n.interconnect();

        for (ModelEntry e : entries)
            e.applyInputs();

        Model m = new Model();
        for (ModelEntry e : entries)
            e.getPart().registerNodes(m);

        return m;
    }

}
