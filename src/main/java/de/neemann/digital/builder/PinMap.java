package de.neemann.digital.builder;

import de.neemann.digital.builder.jedec.FuseMapFillerException;

import java.util.HashMap;

/**
 * A PinMap.
 * Used to assign a symbolic name to a pin number
 *
 * @author hneemann
 */
public class PinMap {
    private final HashMap<String, Integer> pinMap;
    private int[] inputPins;
    private int[] outputPins;

    /**
     * Creates a new instance
     */
    public PinMap() {
        pinMap = new HashMap<>();
    }

    /**
     * Sets the available input pin numbers
     *
     * @param inputPins the input pins
     * @return this for chained calls
     */
    public PinMap setAvailInputs(int... inputPins) {
        this.inputPins = inputPins;
        return this;
    }

    /**
     * Sets the available output pin numbers
     *
     * @param outputPins the input pins
     * @return this for chained calls
     */
    public PinMap setAvailOutputs(int... outputPins) {
        this.outputPins = outputPins;
        return this;
    }

    /**
     * Assigne a symbolic name to a pin
     *
     * @param name the name
     * @param pin  the pin
     * @return this for chained calls
     * @throws FuseMapFillerException FuseMapFillerException
     */
    public PinMap assignPin(String name, int pin) throws FuseMapFillerException {
        if (pinMap.containsKey(name))
            throw new FuseMapFillerException("Pin " + name + " assigned twice");
        if (pinMap.containsValue(pin))
            throw new FuseMapFillerException("Pin " + pin + " assigned twice");
        pinMap.put(name, pin);
        return this;
    }

    private Integer search(int[] pins, String name) {
        for (int i : pins) {
            if (!pinMap.containsValue(i)) {
                pinMap.put(name, i);
                return i;
            }
        }
        return null;
    }

    private boolean contains(int[] pins, int p) {
        for (int i : pins)
            if (i == p) return true;
        return false;
    }

    /**
     * Gets the input pin number for the symbolic name.
     * If no assignment found on of the pins is selected automatically
     *
     * @param in the name
     * @return the  pin number
     * @throws FuseMapFillerException FuseMapFillerException
     */
    public int getInputFor(String in) throws FuseMapFillerException {
        Integer p = pinMap.get(in);
        if (p == null)
            p = search(inputPins, in);
        if (p == null) {
            throw new FuseMapFillerException("to manny inputs defined");
        } else if (!contains(inputPins, p)) {
            throw new FuseMapFillerException("input " + p + " not allowed!");
        }
        return p;
    }

    /**
     * Gets the output pin number for the symbolic name.
     * If no assignment is found on of the pins is selected automatically
     *
     * @param out the name
     * @return the  pin number
     * @throws FuseMapFillerException FuseMapFillerException
     */
    public int getOutputFor(String out) throws FuseMapFillerException {
        Integer p = pinMap.get(out);
        if (p == null)
            p = search(outputPins, out);
        if (p == null) {
            throw new FuseMapFillerException("to manny outputs defined");
        } else if (!contains(outputPins, p)) {
            throw new FuseMapFillerException("output " + p + " not allowed!");
        }
        return p;
    }


}
