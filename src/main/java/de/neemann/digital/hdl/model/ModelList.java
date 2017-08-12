package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A list of models used by the root model
 */
public class ModelList implements Iterable<HDLModel> {
    private HashMap<Circuit, HDLModel> models;
    private ElementLibrary library;

    /**
     * Creates a new instance
     *
     * @param library the library
     */
    public ModelList(ElementLibrary library) {
        this.library = library;
        models = new HashMap<>();
    }

    /**
     * Creates a model from the given circuit
     *
     * @param circuit the circuit
     * @param name    the name of the circuit
     * @return the model
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws HDLException             HDLException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public HDLModel getModel(Circuit circuit, String name) throws PinException, NodeException, HDLException, ElementNotFoundException {
        HDLModel m = models.get(circuit);
        if (m == null) {
            m = new HDLModel(circuit, library, this, false).setName(name);
            models.put(circuit, m);
        }
        return m;
    }

    @Override
    public Iterator<HDLModel> iterator() {
        return models.values().iterator();
    }
}
