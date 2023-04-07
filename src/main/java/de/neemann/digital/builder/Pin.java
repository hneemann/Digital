package de.neemann.digital.builder;

import de.neemann.digital.core.element.PinDescription;

/**
 * Represents a Pin with a number and a direction.
 */
public class Pin {
    private final int num;
    private final PinDescription.Direction direction;

    /**
     * Constructs a new Pin with the given number and direction.
     *
     * @param num       the pin number
     * @param direction the pin direction
     */
    public Pin(int num, PinDescription.Direction direction) {
        this.num = num;
        this.direction = direction;
    }

    /**
     * Returns the pin number.
     *
     * @return the pin number
     */
    public int getNum() {
        return num;
    }

    /**
     * Returns the pin direction.
     *
     * @return the pin direction
     */
    public PinDescription.Direction getDirection() {
        return direction;
    }
}
