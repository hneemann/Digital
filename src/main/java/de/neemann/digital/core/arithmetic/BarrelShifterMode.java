package de.neemann.digital.core.arithmetic;


import de.neemann.digital.core.ObservableValue;

/**
 * @author heintz
 */
public enum BarrelShifterMode {
    /**
     * the default format as defined in {@link ObservableValue#getValueString()}
     */
    normal,
    /**
     * rotate
     */
    rotate,
    /**
     * arithmetic
     */
    arithmetic;
}
