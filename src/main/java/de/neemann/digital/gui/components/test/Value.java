package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.ObservableValue;

/**
 * @author hneemann
 */
public class Value {

    private final long value;
    private final boolean highZ;
    private final boolean dontCare;

    public Value(ObservableValue ov) {
        this.value = ov.getValue();
        this.highZ = ov.isHighZ();
        this.dontCare = false;
    }

    public Value(String val) {
        val = val.toUpperCase().trim();
        if (val.equals("X")) {
            highZ = false;
            value = 0;
            dontCare = true;
        } else {
            dontCare = false;
            if (val.equals("Z")) {
                highZ = true;
                value = 0;
            } else {
                highZ = false;
                value = Long.parseLong(val);
            }
        }
    }

    public boolean isEqualTo(ObservableValue ov) {
        return isEqualTo(new Value(ov));
    }

    public boolean isEqualTo(Value v) {
        if (this == v) return true;
        if (v == null) return false;

        if (dontCare || v.dontCare) return true;

        if (highZ && v.highZ) return true;
        if (highZ != v.highZ) return false;

        return value == v.value;
    }

    @Override
    public String toString() {
        if (dontCare)
            return "X";
        if (highZ)
            return "Z";
        return ObservableValue.getHexString(value);
    }

    public boolean isDontCare() {
        return dontCare;
    }

    public boolean isHighZ() {
        return highZ;
    }

    public long getValue() {
        return value;
    }

    public void setTo(ObservableValue ov) {
        ov.set(value, isHighZ());
    }
}
