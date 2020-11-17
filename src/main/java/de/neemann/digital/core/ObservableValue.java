/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

import java.util.Random;

/**
 * Represents all signal values in the simulator.
 * There are some setters to set the value. Each bit of a value can be set to high z state.
 * Observers can observe this value to be notified if the value changes.
 */
public class ObservableValue extends Observable implements PinDescription {

    private final String name;
    private final long mask;
    private final long signedFlag;
    private final int bits;
    // the value, high z bits are always set to zero
    private long value;
    // the high z state of each bit
    private long highZ;
    private boolean bidirectional;
    private boolean isConstant = false;
    private String description;
    private String pinNumber;
    private boolean isSwitchPin;
    // used to create random bits if high-z values are read
    private Random random;

    /**
     * Creates a new instance.
     *
     * @param name the name of this value
     * @param bits the number of bits
     */
    public ObservableValue(String name, int bits) {
        this.name = name;
        this.bits = bits;
        mask = Bits.mask(bits);
        signedFlag = Bits.signedFlagMask(bits);
    }


    /**
     * Makes this value a constant value
     *
     * @return this for chained calls
     */
    public ObservableValue setConstant() {
        isConstant = true;
        return this;
    }

    /**
     * @return true if this value is a constant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * Sets the value and fires an event if the value has changed.
     * Also sets all bits to low Z.
     *
     * @param value the new value
     * @return this for chained calls
     */
    public ObservableValue setValue(long value) {
        return set(value, 0);
    }

    /**
     * Sets all bits to highZ state.
     *
     * @return this for chained calls
     */
    public ObservableValue setToHighZ() {
        return set(0, -1);
    }

    /**
     * Sets the value and highZ state and fires an event if value has changed.
     *
     * @param value the value
     * @param highZ highZ state
     * @return this for chained calls
     */
    public ObservableValue set(long value, long highZ) {
        value = getValueBits(value);
        highZ = getValueBits(highZ);
        if (highZ != this.highZ || ((~highZ & (value ^ this.value))) != 0) {

            if (isConstant)
                throw new RuntimeException("tried to modify a constant value!");

            this.highZ = highZ;
            this.value = value & (~highZ);  // high Z bits are set to zero
            fireHasChanged();
        }
        return this;
    }

    /**
     * Adds an observer to this value.
     *
     * @param observer the observer to add
     * @return this for chained calls
     */
    public ObservableValue addObserverToValue(Observer observer) {
        addObserver(observer);
        return this;
    }

    /**
     * @return the number of bits of this value
     */
    public int getBits() {
        return bits;
    }

    /**
     * Returns the current value.
     * The high-z bits are set to a random value.
     *
     * @return the value
     */
    public long getValue() {
        if (highZ != 0) {
            if (random == null)
                random = new Random();
            return value | (random.nextLong() & highZ);
        } else
            return value;
    }

    /**
     * Returns the current value
     * The high-z bits are set to zero
     *
     * @return the value
     */
    public long getValueHighZIsZero() {
        return value;
    }

    /**
     * returns the actual high z bit mask
     *
     * @return the high z bit mask
     */
    public long getHighZ() {
        return highZ;
    }

    /**
     * returns the actual value as a string
     *
     * @return the value as string
     */
    public String getValueString() {
        if (highZ != 0)
            if (highZ == mask)
                return "Z";
            else {
                return zMaskString(value, highZ, bits);
            }
        else {
            return IntFormat.toShortHex(value);
        }
    }

    static String zMaskString(long value, long highZ, int bits) {
        StringBuilder sb = new StringBuilder();
        long m = Bits.up(1, bits - 1);
        for (int i = 0; i < bits; i++) {
            if ((highZ & m) != 0) {
                sb.append("z");
            } else {
                if ((value & m) != 0) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
            m >>>= 1;
        }
        return sb.toString();
    }

    /**
     * returns the actual value as a signed long
     *
     * @return the signed value
     */
    public long getValueSigned() {
        long v = getValue();
        if ((v & signedFlag) != 0) v |= ~mask;
        return v;
    }

    /**
     * Returns the value as a bool.
     *
     * @return the boolean
     */
    public boolean getBool() {
        return getValue() != 0;
    }

    /**
     * Sets a bool value.
     *
     * @param bool the boolean to set
     */
    public void setBool(boolean bool) {
        if (bool)
            setValue(1);
        else
            setValue(0);
    }

    /**
     * reduces a given value to the number of bits used by this value.
     *
     * @param value the value to reduce
     * @return the reduced value
     */
    public long getValueBits(long value) {
        return value & mask;
    }

    /**
     * checks if the given number of bits is the same used by this value.
     * It is a convenience method to make this check simpler to code.
     *
     * @param bits the number of bits
     * @param node the node to add to the exception if one is thrown
     * @return this for chained calls
     * @throws BitsException thrown if bit numbers do not match
     */
    public ObservableValue checkBits(int bits, Node node) throws BitsException {
        return checkBits(bits, node, -1);
    }

    /**
     * checks if the given number of bits is the same used by this value.
     * It is a convenience method to make this check simpler to code.
     *
     * @param bits  the number of bits
     * @param node  the node to add to the exception if one is thrown
     * @param input the affected nodes input
     * @return this for chained calls
     * @throws BitsException thrown if bit numbers do not match
     */
    public ObservableValue checkBits(int bits, Node node, int input) throws BitsException {
        if (this.bits != bits) {
            throw new BitsException(Lang.get("err_needs_N0_bits_found_N2_bits", bits, this.bits), node, input, this);
        }
        return this;
    }

    /**
     * @return true if one of the bits is in high z state
     */
    public boolean isHighZ() {
        return highZ != 0;
    }

    @Override
    public String toString() {
        return name + "{"
                + "value=" + getValueString()
                + ", setBits=" + bits
                + '}';
    }

    /**
     * @return the name of this value
     */
    public String getName() {
        return name;
    }

    /**
     * Makes this value a bidirectional value.
     *
     * @return this for chained calls
     */
    public ObservableValue setBidirectional() {
        this.bidirectional = true;
        return this;
    }

    @Override
    public String getDescription() {
        if (description != null)
            return description;
        else
            return getName();
    }

    /**
     * Sets the description of this value.
     *
     * @param description the description
     * @return this for call chaining
     */
    public ObservableValue setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public Direction getDirection() {
        if (bidirectional)
            return Direction.both;
        else
            return Direction.output;
    }

    /**
     * Returns a list containing only this {@link ObservableValue}
     *
     * @return the list
     */
    public ObservableValues asList() {
        return new ObservableValues(this);
    }

    /**
     * sets this description to the appropriate language entry which is determined by the
     * given descriptions language key.
     *
     * @param description the {@link ElementTypeDescription}
     * @return this for chained calls
     */
    public ObservableValue setPinDescription(ElementTypeDescription description) {
        setDescription(Lang.get(description.getPinLangKey() + name));
        return this;
    }

    @Override
    public String getPinNumber() {
        return pinNumber;
    }

    @Override
    public boolean isClock() {
        return false;  // output pins are never clock pins
    }

    @Override
    public boolean isSwitchPin() {
        return isSwitchPin;
    }

    /**
     * Flags this output value as a switch output
     *
     * @param switchPin true is switch pin
     * @return this for chained calls
     */
    public ObservableValue setSwitchPin(boolean switchPin) {
        isSwitchPin = switchPin;
        return this;
    }

    /**
     * Sets the pin number
     *
     * @param pinNumber the pin number
     * @return this for chained calls
     */
    public ObservableValue setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
        return this;
    }

    /**
     * @return a copy of this value
     */
    public Value getCopy() {
        return new Value(this);
    }
}
