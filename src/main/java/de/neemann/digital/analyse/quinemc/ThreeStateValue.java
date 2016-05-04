package de.neemann.digital.analyse.quinemc;

/**
 * @author hneemann
 */
public enum ThreeStateValue {
    one,
    zero,
    dontCare;


    public static ThreeStateValue value(boolean bool) {
        if (bool) {
            return one;
        } else {
            return zero;
        }
    }

    public static ThreeStateValue value(int value) {
        switch (value) {
            case 0:
                return ThreeStateValue.zero;
            case 1:
                return ThreeStateValue.one;
            default:
                return ThreeStateValue.dontCare;
        }
    }
}
