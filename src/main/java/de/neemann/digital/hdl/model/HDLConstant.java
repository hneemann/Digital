package de.neemann.digital.hdl.model;

import de.neemann.digital.lang.Lang;

/**
 * Represets a hdl constant value
 */
public class HDLConstant {

    /**
     * Possiple types
     */
    public enum Type {
        /**
         * weak high, in digital represented by a pull up
         */
        weakHigh,
        /**
         * weak low, in digital represented by a pull down
         */
        weakLow,
        /**
         * high z
         */
        highz,
        /**
         * don't care value
         */
        dontcare,
        /**
         * a normal number
         */
        normal
    }

    private final Type type;
    private final int bits;
    private final long value;

    /**
     * Creates a new value like "don't care" or "highz"
     *
     * @param type the type
     * @param bits number of bits
     */
    public HDLConstant(Type type, int bits) {
        this.type = type;
        this.bits = bits;
        value = 0;
    }

    /**
     * Creates a new numeric value
     *
     * @param value the value
     * @param bits  number of bits
     */
    public HDLConstant(long value, int bits) {
        type = Type.normal;
        this.value = value;
        this.bits = bits;
    }

    /**
     * returns a VHDL specific representation of the constant
     *
     * @return the string  representation
     * @throws HDLException HDLException
     */
    public String vhdlValue() throws HDLException {
        if (type == Type.normal) {
            long val = value & ((1 << bits) - 1);
            if (bits > 1) {
                StringBuilder sb = new StringBuilder("\"");
                String str = Long.toBinaryString(val);
                for (int i = bits - str.length(); i > 0; i--)
                    sb.append('0');
                sb.append(str).append("\"");
                return sb.toString();
            } else
                return "'" + val + "'";
        } else {
            char c = getVHDLChar();
            if (bits > 1) {
                StringBuilder sb = new StringBuilder("\"");
                for (int i = 0; i < bits; i++)
                    sb.append(c);
                sb.append("\"");
                return sb.toString();

            } else
                return "'" + c + "'";
        }
    }

    private char getVHDLChar() throws HDLException {
        switch (type) {
            case weakLow:
                return 'L';
            case weakHigh:
                return 'H';
            case dontcare:
                return '-';
            case highz:
                return 'Z';
            default:
                throw new HDLException(Lang.get("err_vhdlUnknownConstantType_N", type));
        }
    }
}


