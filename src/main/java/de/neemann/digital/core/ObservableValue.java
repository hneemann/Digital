package de.neemann.digital.core;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ObservableValue extends Value {

    private final ArrayList<Listener> listeners;
    private final String name;

    public ObservableValue(String name, int bits) {
        this(name, bits, false);
    }

    public ObservableValue(String name, int bits, boolean highZ) {
        super(bits, highZ);
        this.name = name;
        listeners = new ArrayList<>();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void hasChanged() {
        for (Listener l : listeners) {
            l.needsUpdate();
        }
    }

    public int getBits() {
        return bits;
    }

    public long getValue() throws NodeException {
        if (highZ)
            throw new HighZException(this);
        return value;
    }

    public void setValue(long value) {
        if (this.value != value) {
            this.value = value;
            if (!highZ)
                hasChanged();
        }
    }

    public long getValueBits() throws NodeException {
        return getValueBits(getValue());
    }

    public long getValueBits(long value) {
        return value & (1 << bits) - 1;
    }

    public void checkBits(ObservableValue value) throws BitsException {
        if (value.getBits() != bits) {
            throw new BitsException("needs " + bits + " bits, found " + value.getBits(), this, value);
        }
    }

    public boolean isHighZ() {
        return highZ;
    }

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
        return name + "{" +
                "value=" + (highZ ? "??" : value) +
                ", bits=" + bits +
                '}';
    }

    public String getName() {
        return name;
    }
}
