package de.neemann.digital.core.element;

/**
 * Description of a pin
 *
 * @author hneemann
 */
public interface PinDescription {

    /**
     * The possible pull resistor configurations
     * "both" is an error condition which can happen if nets are merged
     */
    enum PullResistor {
        none, pullUp, pullDown, both
    }

    /**
     * The possible directions of a pin
     */
    enum Direction {
        input, output, both
    }

    /**
     * @return the pins name
     */
    String getName();

    /**
     * @return the pins description
     */
    String getDescription();

    /**
     * @return the Pins direction
     */
    Direction getDirection();

    /**
     * @return The possible pull resistor configuration of this pin
     */
    default PullResistor getPullResistor() {
        return PullResistor.none;
    }

    /**
     * @return the pin number
     */
    String getPinNumber();

    /**
     * Returns true if this pin is a cock pin.
     * Is used only to draw the small triangle in front of the pins label.
     *
     * @return true if this pin is a clock input
     */
    boolean isClock();
}
