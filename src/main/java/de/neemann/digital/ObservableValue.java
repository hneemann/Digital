package de.neemann.digital;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ObservableValue {

    private final int bits;
    private int value;
    private ArrayList<Listener> listeners;

    public ObservableValue(int bits) {
        this.bits = bits;
        listeners = new ArrayList<>();
    }

    public void addListener(Listener listener) throws NodeException {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void hasChanged() throws NodeException {
        for (Listener l : listeners) {
            l.needsUpdate();
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) throws NodeException {
        if (this.value != value) {
            this.value = value;
            hasChanged();
        }
    }

    public int getBits() {
        return bits;
    }

    public int getValueBits() {
        return getValueBits(value);
    }

    public int getValueBits(int value) {
        return value & (1 << bits) - 1;
    }

    public void checkBits(ObservableValue value) throws BitsException {
        if (value.getBits() != bits) {
            throw new BitsException("needs " + bits + " bits, found " + value.getBits());
        }
    }

    @Override
    public String toString() {
        return "ObservableValue{" +
                "value=" + value +
                ", bits=" + bits +
                '}';
    }
}
