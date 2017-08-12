package de.neemann.digital.hdl.model;

import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A list of ports
 */
public class Ports implements Iterable<Port> {
    private ArrayList<Port> ports;
    private ArrayList<Port> outputs;
    private ArrayList<Port> inputs;
    private HashSet<String> lowerCaseNames;

    /**
     * creates a new instance
     */
    public Ports() {
        ports = new ArrayList<>();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        lowerCaseNames = new HashSet<>();
    }

    @Override
    public Iterator<Port> iterator() {
        return ports.iterator();
    }

    /**
     * Adds a port to this list
     *
     * @param port the port to add
     * @return this for chained calls
     * @throws HDLException if name is used twice
     */
    public Ports add(Port port) throws HDLException {
        String lowerCaseName = port.getName().toLowerCase();
        if (lowerCaseNames.contains(lowerCaseName))
            throw new HDLException(Lang.get("err_nameUsedTwice_N", port.getName()));
        lowerCaseNames.add(lowerCaseName);

        ports.add(port);
        if (port.getDirection() == Port.Direction.in)
            inputs.add(port);
        else
            outputs.add(port);
        return this;
    }

    @Override
    public String toString() {
        return ports.toString();
    }

    /**
     * @return the number of stored ports
     */
    public int size() {
        return ports.size();
    }

    /**
     * Returns the port with the number i
     *
     * @param i the number
     * @return the port i
     */
    public Port get(int i) {
        return ports.get(i);
    }

    /**
     * @return the list of outputs
     */
    public ArrayList<Port> getOutputs() {
        return outputs;
    }

    /**
     * @return the list of inputs
     */
    public ArrayList<Port> getInputs() {
        return inputs;
    }

}
