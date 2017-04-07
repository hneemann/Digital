package de.neemann.digital.draw.library;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.lang.Lang;

import java.io.File;

/**
 * This class represents a custom, nested element.
 * So it is possible to use an element in the circuit witch is made from an
 * existing circuit. So you can build hierarchical circuits.
 *
 * @author hneemann
 */
public class CustomElement implements Element {
    private static final int MAX_DEPTH = 30;

    private final Circuit circuit;
    private final ElementLibrary library;
    private final File name;
    private NetList netList;

    /**
     * Creates a new custom element
     *
     * @param circuit the inner circuit
     * @param library the library to use.
     * @param name    the name of the element
     */
    public CustomElement(Circuit circuit, ElementLibrary library, File name) {
        this.circuit = circuit;
        this.library = library;
        this.name = name;
    }

    /**
     * Gets a {@link ModelCreator} of this circuit.
     * Every time this method is called a new {@link ModelCreator} is created.
     *
     * @param subName name of the circuit, used to name unique elements
     * @param depth   recursion depth, used to detect a circuit which contains itself
     * @return the {@link ModelCreator}
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator getModelDescription(String subName, int depth) throws PinException, NodeException, ElementNotFoundException {
        if (netList == null)
            netList = new NetList(circuit);

        if (depth > MAX_DEPTH)
            throw new NodeException(Lang.get("err_recursiveNestingAt_N0", name.getName()));

        return new ModelCreator(circuit, library, true, name, new NetList(netList), subName, depth);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new RuntimeException("invalid call!");
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return circuit.getOutputNames();
    }

    @Override
    public void registerNodes(Model model) {
        throw new RuntimeException("invalid call!");
    }
}
