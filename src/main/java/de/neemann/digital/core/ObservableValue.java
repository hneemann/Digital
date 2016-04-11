package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class ObservableValue extends Value {

    private final ArrayList<Observer> observers;
    private final String name;
    private final long mask;
    private final boolean supportsHighZ;
    private boolean bidirectional;

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
        super(bits, highZ);
        mask = (1L << bits) - 1;
        this.name = name;
        observers = new ArrayList<>();
        supportsHighZ = highZ;
    }

    /**
     * Adds an observer to this value.
     *
     * @param observer the observer to add
     * @return this for call chaining
     */
    public ObservableValue addObserver(Observer observer) {
        if (observer != null)
            observers.add(observer);
        return this;
    }

    /**
     * Removes an observer from this value.
     *
     * @param observer the observer to use
     */
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Removes al observers from the given class
     *
     * @param observerClass the class of observers to remove
     */
    public void removeObserver(Class<? extends Observer> observerClass) {
        Iterator<Observer> it = observers.iterator();
        while (it.hasNext()) {
            if (it.next().getClass() == observerClass)
                it.remove();
        }
    }

    /**
     * Fires a has changed event to all observers
     */
    public void hasChanged() {
        for (Observer l : observers) {
            l.hasChanged();
        }
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
//        if (highZ)
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
     * Sets the value and fires a event if value has changed.
     *
     * @param value the new value
     */
    public void setValue(long value) {
        value = getValueBits(value);
        if (this.value != value) {
            this.value = value;
            if (!highZ)
                hasChanged();
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
        setValue(bool ? 1 : 0);
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


    @Override
    public void set(long value, boolean highZ) {
        setValue(value);
        setHighZ(highZ);
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
     * @return the numbers of observers
     */
    public int observerCount() {
        return observers.size();
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
}
