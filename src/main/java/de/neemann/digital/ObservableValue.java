package de.neemann.digital;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ObservableValue extends Value {

    private final ArrayList<Listener> listeners;

    public ObservableValue(int bits) {
        this(bits, false);
    }

    public ObservableValue(int bits, boolean highZ) {
        super(bits, highZ);
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

    public int getValue() throws NodeException {
        if (highZ)
            throw new HighZException(this);
        return value;
    }

    public void setValue(int value) {
        if (this.value != value) {
            this.value = value;
            if (!highZ)
                hasChanged();
        }
    }

    public int getValueBits() throws NodeException {
        return getValueBits(getValue());
    }

    public int getValueBits(int value) {
        return value & (1 << bits) - 1;
    }

    public void checkBits(ObservableValue value) throws BitsException {
        if (value.getBits() != bits) {
            throw new BitsException("needs " + bits + " bits, found " + value.getBits());
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
    public void set(int value, boolean highZ) {
        setValue(value);
        setHighZ(highZ);
    }

    @Override
    public String toString() {
        return "ObservableValue{" +
                "value=" + (highZ ? "??" : value) +
                ", bits=" + bits +
                '}';
    }
}
