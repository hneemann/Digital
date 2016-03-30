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

    public ObservableValue(String name, int bits) {
        this(name, bits, false);
    }

    public ObservableValue(String name, int bits, boolean highZ) {
        super(bits, highZ);
        mask = (1L << bits) - 1;
        this.name = name;
        observers = new ArrayList<>();
        supportsHighZ = highZ;
    }

    public ObservableValue addObserver(Observer observer) {
        if (observer != null)
            observers.add(observer);
        return this;
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void removeObserver(Class<? extends Observer> observerClass) {
        Iterator<Observer> it = observers.iterator();
        while (it.hasNext()) {
            if (it.next().getClass() == observerClass)
                it.remove();
        }
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
//        if (highZ)
//            throw new HighZException(this);
        return value;
    }

    public String getValueString() {
        if (highZ)
            return "?";
        else {
            return getHexString(value);
        }
    }

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

    public ObservableValue checkBits(int bits, Node node) throws BitsException {
        if (this.bits != bits) {
            throw new BitsException(Lang.get("err_needs_N0_bits_found_N2_bits", bits, this.bits), node, this);
        }
        return this;
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
                "value=" + getValueString() +
                ", setBits=" + bits +
                '}';
    }

    public String getName() {
        return name;
    }

    public int observerCount() {
        return observers.size();
    }

    public boolean supportsHighZ() {
        return supportsHighZ;
    }

    public boolean isHighZIgnoreBurn() {
        return highZ;
    }

    public long getValueIgnoreBurn() {
        return value;
    }

    public ObservableValue setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
        return this;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }
}
