/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

/**
 * Description of a pin
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
        input, output, both;

        /**
         * Returns true if one direction is output and the other is input
         *
         * @param a direction a
         * @param b direction a
         * @return true if a and b are a input and a output
         */
        public static boolean isInOut(Direction a, Direction b) {
            return (a.equals(input) && b.equals(output)) || (a.equals(output) && b.equals(input));
        }
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

    /**
     * @return true if this pin is a switch pin
     */
    boolean isSwitchPin();
}
