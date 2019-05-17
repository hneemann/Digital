/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.ide;

/**
 * Represents a command to execute
 */
public class Command {
    private final String name;
    private final boolean filter;
    private final String requires;
    private final String[] args;
    private final int timeout;

    /**
     * Creates a new command
     *
     * @param name     the name of the command
     * @param requires the hdl which is required, either "verilog" of "vhdl"
     * @param filter   the true, the commands args are filtered
     * @param gui      if true the appp has a gui
     * @param timeout  the timeout value in sec
     * @param args     the arguments
     */
    public Command(String name, String requires, boolean filter, boolean gui, int timeout, String... args) {
        this.name = name;
        this.requires = requires;
        this.timeout = timeout;
        this.args = args;
        this.filter = filter;
    }

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
        return getHDL().length() > 0;
    }

    /**
     * @return the hdl which is required, either "verilog" of "vhdl"
     */
    public String getHDL() {
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
