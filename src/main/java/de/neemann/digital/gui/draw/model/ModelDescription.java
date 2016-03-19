package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartTypeDescription;
import de.neemann.digital.gui.draw.library.PartLibrary;
import de.neemann.digital.gui.draw.parts.*;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ModelDescription {

    private final Circuit circuit;
    private final NetList netList;
    private final ArrayList<ModelEntry> entries;

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
     * @param guiObserver can be used to update the GUI by calling hasChanged, maybe null
     * @return the model
     * @throws PinException
     * @throws NodeException
     */
    public Model createModel(Observer guiObserver) throws PinException, NodeException {
        for (Net n : netList)
            n.interconnect();

        Model m = new Model();

        for (ModelEntry e : entries)
            e.applyInputs(guiObserver, m);

        for (ModelEntry e : entries)
            e.getPart().registerNodes(m);

        return m;
    }

}
