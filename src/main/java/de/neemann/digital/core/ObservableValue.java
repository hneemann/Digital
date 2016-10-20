package de.neemann.digital.core;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class ObservableValue extends Observable implements PinDescription {

    private final String name;
    private final long mask;
    private final boolean supportsHighZ;
    private final int bits;
    private long value;
    private boolean highZ;
    private boolean bidirectional;
    private String description;

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
        mask = (1L << bits) - 1;
        this.name = name;
        supportsHighZ = highZ;
    }

    /**
     * Sets the value and fires a event if value has changed.
     *
     * @param value the new value
     * @return this for chained calls
     */
    public ObservableValue setValue(long value) {
        value = getValueBits(value);
        if (this.value != value) {
            this.value = value;
            if (!highZ)
                hasChanged();
        }
        return this;
    }

    /**
     * Sets the highZ state of this value
     *
     * @param highZ the new highZ state
     */
    public void setHighZ(boolean highZ) {
        if (this.highZ != highZ) {
            this.highZ = highZ;
            hasChanged();
        }
    }

    /**
     * Sets the value and highZ state
     *
     * @param value the value
     * @param highZ highZ state
     */
    public void set(long value, boolean highZ) {
        setValue(value);
        setHighZ(highZ);
    }

    /**
     * Adds an observer to this value.
     *
     * @param observer the observer to add
     * @return this for chained calls
     */
    public ObservableValue addObserverToValue(Observer observer) {
        super.addObserver(observer);
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
//        if (highZ)      // ToDo: how to handle highZ read?
//            throw new HighZException(this);
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
            return getHexString(value);
        }
    }

    /**
     * converts a value to a minimal hex string
     *
     * @param value the value
     * @return the string representation
     */
    public static String getHexString(long value) {
        String s = Long.toHexString(value).toUpperCase();
        if (s.length() == 1)
            return s;
        else {
            boolean mark = true;
            for (int i = 0; i < s.length(); i++)
                if (s.charAt(i) > '9') {
                    mark = false;
                    break;
                }
            if (mark)
                return "0x" + s;
            else
                return s;
        }
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
        if (this.bits != bits) {
            throw new BitsException(Lang.get("err_needs_N0_bits_found_N2_bits", bits, this.bits), node, this);
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
    public long getValueIgnoreBurn() {
        return value;
    }

    /**
     * Makes this value a bidirectional value.
     *
     * @param bidirectional true if value is bidirectional
     * @return this for chained calls
     */
    public ObservableValue setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
        return this;
    }

    /**
     * @return true if value is bidirectional
     */
    public boolean isBidirectional() {
        return bidirectional;
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
     * @param descriptionKey the descriptions key
     * @return this for call chaining
     */
    public ObservableValue setDescriptionKey(String descriptionKey) {
        return setDescription(Lang.get(descriptionKey));
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

}
