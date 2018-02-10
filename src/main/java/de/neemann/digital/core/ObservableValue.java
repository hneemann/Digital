package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

/**
 * Represents all signal values in the simulator.
 * There are some setters to set the value. A value can be set to high z state.
 * Only a complete bus can be set to high z state. It is not possible to set
 * a single bit of a bus to high z state.
 * Observers can observe this value to be notified if the value changes.
 *
 * @author hneemann
 */
public class ObservableValue extends Observable implements PinDescription {

    private final String name;
    private final long mask;
    private final long signedFlag;
    private final boolean supportsHighZ;
    private final int bits;
    private long value;
    private boolean highZ;
    private boolean bidirectional;
    private boolean isConstant = false;
    private String description;
    private String pinNumber;

    /**
     * Creates a new instance
     *
     * @param name the name of this value
     * @param bits the number of bits
     */
    public ObservableValue(String name, int bits) {
        this(name, bits, false);
    }

    /**
     * Creates a new instance.
     *
     * @param name  the name of this value
     * @param bits  the number of bits
     * @param highZ if true this value can be a high impedance value
     */
    public ObservableValue(String name, int bits, boolean highZ) {
        super();
        this.bits = bits;
        this.highZ = highZ;
        mask = Bits.mask(bits);
        signedFlag = Bits.signedFlagMask(bits);
        this.name = name;
        supportsHighZ = highZ;
    }


    /**
     * Makes this value a constant value
     */
    public void setConstant() {
        isConstant = true;
    }

    /**
     * @return true if this value is a constant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * Sets the value and fires an event if value has changed.
     * Also sets this value to low Z
     *
     * @param value the new value
     * @return this for chained calls
     */
    public ObservableValue setValue(long value) {
        set(value, false);
        return this;
    }

    /**
     * Sets the value and highZ state and fires an event if value has changed.
     *
     * @param value the value
     * @param highZ highZ state
     * @return this for chained calls
     */
    public ObservableValue set(long value, boolean highZ) {
        value = getValueBits(value);
        if (highZ != this.highZ || (!highZ && (value != this.value))) {

            if (isConstant)
                throw new RuntimeException("tried to modify a constant value!");

            this.highZ = highZ;
            this.value = value;
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
     * returns the actual value
     *
     * @return the value
     */
    public long getValue() {
        if (highZ)      // ToDo: how to handle highZ read?
            return 0;
        else
            return value;
    }

    /**
     * returns the actual value as a string
     *
     * @return the value as string
     */
    public String getValueString() {
        if (highZ)
            return "?";
        else {
            return IntFormat.toShortHex(value);
        }
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
     * @return true if this value is a high z value
     */
    public boolean isHighZ() {
        return highZ;
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
     * @return returns true if the value could become a highZ value
     */
    public boolean supportsHighZ() {
        return supportsHighZ;
    }

    /**
     * Returns the value and does not throw a highZ exception.
     * Should be used if the value is needed to create a graphical representation to
     * avoid the graphical representation is causing exceptions.
     *
     * @return the actual value.
     */
    public long getValueIgnoreHighZ() {
        return value;
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
