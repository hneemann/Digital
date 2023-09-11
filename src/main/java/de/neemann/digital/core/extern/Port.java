/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.Bits;
import de.neemann.digital.hdl.hgs.HGSMap;

/**
 * A port for external access
 */
public class Port implements HGSMap {
    private final int bits;
    private String name;
    private boolean bidirectional = false;

    /**
     * Creates a new port
     *
     * @param name the name
     * @param bidirectional is Port are bidirectional
     * @param bits the number of bits
     */
    public Port(String name, int bits, boolean bidirectional) {
        this(name, bits);
        this.bidirectional = bidirectional;
    }

    /**
     * Creates a new port
     *
     * @param name the name
     * @param bits the number of bits
     */
    public Port(String name, int bits) {
        this.name = name;
        this.bits = bits;
    }

    /**
     * Creates a new port
     *
     * @param port the port
     */
    public Port(String port) {
        int p = port.indexOf(':');
        if (p < 0) {
            name = port;
            bits = 1;
        } else {
            name = port.substring(0, p);
            int b = 1;
            try {
                b = (int) Bits.decode(port.substring(p + 1));
            } catch (Bits.NumberFormatException e) {
                b = 1;
            }
            bits = b;
        }
        if (name.startsWith("+")) {
            bidirectional = true;
            name = name.substring(1);
        }
    }

    /**
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * @return the name of the port
     */
    public String getName() {
        return name;
    }

    /**
     * @return is port are bidirectional
     */
    public boolean isBidirectional() {
        return bidirectional;
    }

    @Override
    public String toString() {
        if (bits == 1)
            return (bidirectional ? "+" : "") + name;
        else
            return (bidirectional ? "+" : "") + name + ":" + bits;
    }
    @Override
    public Object hgsMapGet(String key) {
        switch (key) {
            case "name":
                return name;
            case "bits":
                return bits;
            default:
                return null;
        }
    }
}
