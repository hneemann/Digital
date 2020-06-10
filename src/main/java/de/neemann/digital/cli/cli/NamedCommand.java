/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

/**
 * The base class of all named commands
 */
public abstract class NamedCommand implements CLICommand {
    private final String name;

    /**
     * Create a new instance
     *
     * @param name the name of the command
     */
    public NamedCommand(String name) {
        this.name = name;
    }

    /**
     * @return the name of the command
     */
    public String getName() {
        return name;
    }
}
