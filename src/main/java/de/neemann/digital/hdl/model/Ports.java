package de.neemann.digital.hdl.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list of ports
 */
public class Ports implements Iterable<Port> {
    private ArrayList<Port> ports;
    private ArrayList<Port> outputs;
    private ArrayList<Port> inputs;

    /**
     * creates a new instance
     */
    public Ports() {
        ports = new ArrayList<>();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
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
     */
    public Ports add(Port port) {
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
