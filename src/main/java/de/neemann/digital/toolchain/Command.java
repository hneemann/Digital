/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

/**
 * Represents a command to execute
 */
public class Command {
    private String name;
    private boolean filter;
    private String requires;
    private String[] args;
    private int timeout;

    /**
     * @return the commands name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the commands args
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * @return true if a hdl is required
     */
    public boolean needsHDL() {
        return getHDL() != null && getHDL().length() > 0;
    }

    /**
     * @return the hdl which is required, either "verilog" or "vhdl"
     */
    public String getHDL() {
        if (requires == null)
            return null;
        return requires.trim().toLowerCase();
    }

    /**
     * @return true if the arguments needs to be filtered
     */
    public boolean isFilter() {
        return filter;
    }

    /**
     * @return timeout in seconds
     */
    public int getTimeout() {
        return timeout;
    }
}
