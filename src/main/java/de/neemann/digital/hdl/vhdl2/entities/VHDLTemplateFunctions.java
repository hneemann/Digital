/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2.entities;

import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Value;
import de.neemann.digital.hdl.vhdl2.VHDLCreator;

import java.util.ArrayList;

/**
 * Helper functions for the vhdl template generator.
 * The public methods are mapped to the vhdl templates.
 */
public final class VHDLTemplateFunctions {

    private ArrayList<Generic> generics;

    /**
     * Creates a new instance
     */
    VHDLTemplateFunctions() {
        generics = new ArrayList<>();
    }

    /**
     * Create a vhdl zero with the given bit number
     *
     * @param bits the bit number
     * @return '0' or (others => '0')
     */
    public static String zero(long bits) {
        if (bits == 1)
            return "'0'";
        else
            return "(others => '0')";
    }

    /**
     * Creates a vhdl value
     *
     * @param val  the value
     * @param bits the bit number
     * @return the value as vhdl code
     */
    public static String value(long val, long bits) {
        return VHDLCreator.value(val, (int) bits);
    }

    /**
     * Creates the code for a generic type
     *
     * @param n the number of bits
     * @return the type
     */
    public static String genericType(long n) {
        if (n == 1)
            return "std_logic";
        else
            return "std_logic_vector ((Bits-1) downto 0)";
    }

    /**
     * Creates a type of given width
     *
     * @param n the number of bits
     * @return the type
     */
    public static String type(long n) {
        if (n == 1)
            return "std_logic";
        else
            return "std_logic_vector (" + (n - 1) + " downto 0)";
    }

    /**
     * Registers a generic value of the given type
     *
     * @param args the arguments
     * @throws HGSEvalException HGSEvalException
     */
    public void registerGeneric(String... args) throws HGSEvalException {
        if (args.length == 1)
            generics.add(new Generic(args[0], "integer"));
        else if (args.length == 2)
            generics.add(new Generic(args[0], args[1]));
        else
            throw new HGSEvalException("wrong number of arguments");
    }


    ArrayList<Generic> getGenerics() {
        return generics;
    }

    /**
     * A generic value
     */
    public static final class Generic {
        private final String name;
        private final String type;

        private Generic(String name, String type) {
            this.name = name;
            this.type = type.toLowerCase();
        }

        /**
         * @return the name of the generic value
         */
        public String getName() {
            return name;
        }

        /**
         * Formats the generic value according to the values type
         *
         * @param val the value
         * @return the formatted vhdl value
         * @throws HGSEvalException HGSEvalException
         */
        public String format(Object val) throws HGSEvalException {
            switch (type) {
                case "integer":
                    return Long.toString(Value.toLong(val));
                case "real":
                    return Double.toString(Value.toDouble(val));
                case "std_logic":
                    return "'" + (Value.toBool(val) ? 1 : 0) + "'";
                default:
                    throw new HGSEvalException("type " + type + " not allowed as generic");
            }
        }
    }

}
