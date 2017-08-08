package de.neemann.digital.hdl.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list of ports
 */
public class Ports implements Iterable<Port> {
    private ArrayList<Port> ports;

    /**
     * creates a new instance
     */
    public Ports() {
        ports = new ArrayList<>();
    }

    @Override
    public Iterator<Port> iterator() {
        return ports.iterator();
    }

    /**
     * Adds a port to this list
     *
     * @param port the port to add
     */
    public void add(Port port) {
        ports.add(port);
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
}
