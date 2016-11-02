package de.neemann.digital.core.element;

/**
 * Description of a pin
 *
 * @author hneemann
 */
public interface PinDescription {

    /**
     * The possible pull resistor configurations
     */
    enum PullResistor {
        none, pullUp, pullDown
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

}
