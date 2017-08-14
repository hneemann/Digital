package de.neemann.digital.hdl.model;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;

/**
 * Represents a special node inserted in the vhdl model the reduce the clock speed.
 */
public class HDLClockNode extends HDLNode {
    private static final Key<Integer> COUNTER_KEY = new Key<>("maxCounter", 0);
    private final int maxCounter;

    /**
     * creates a new instanze
     *
     * @param maxCounter the max counter value
     * @param ports      the ports of the node
     * @throws HDLException if name is not valid
     */
    public HDLClockNode(int maxCounter, Ports ports) throws HDLException {
        super(ports, "simpleClockDivider",
                new ElementAttributes()
                        .set(COUNTER_KEY, maxCounter));
        this.maxCounter = maxCounter;
    }

    /**
     * @return the max counter value
     */
    public int getmaxCounter() {
        return maxCounter;
    }
}
