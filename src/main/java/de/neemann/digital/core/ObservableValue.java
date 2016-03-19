package de.neemann.digital.core;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ObservableValue extends Value {

    private final ArrayList<Observer> observers;
    private final String name;
    private final long mask;

    public ObservableValue(String name, int bits) {
        this(name, bits, false);
    }

    public ObservableValue(String name, int bits, boolean highZ) {
        super(bits, highZ);
        mask = (1L << bits) - 1;
        this.name = name;
        observers = new ArrayList<>();
    }

    public ObservableValue addObserver(Observer observer) {
        observers.add(observer);
        return this;
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void hasChanged() {
        for (Observer l : observers) {
            l.hasChanged();
        }
    }

    public int getBits() {
        return bits;
    }

    public long getValue() {
        if (highZ)
            throw new HighZException(this);
        return value;
    }

    public void setValue(long value) {
        value = getValueBits(value);
        if (this.value != value) {
            this.value = value;
            if (!highZ)
                hasChanged();
        }
    }

    public boolean getBool() {
        return getValue() != 0;
    }

    public void setBool(boolean bool) {
        setValue(bool ? 1 : 0);
    }

    public long getValueBits(long value) {
        return value & mask;
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
                ", setBits=" + bits +
                '}';
    }

    public String getName() {
        return name;
    }

}
