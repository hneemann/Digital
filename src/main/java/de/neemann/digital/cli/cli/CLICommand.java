/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

import java.io.PrintStream;

/**
 * A cli command
 */
public interface CLICommand {

    /**
     * Prints the description
     *
     * @param out    the pront stream
     * @param prefix the prefex string which should
     *               printed at the beginning of each line
     */
    void printDescription(PrintStream out, String prefix);

    /**
     * Esecuted the command
     *
     * @param args the arguments
     * @throws CLIException CLIException
     */
    void execute(String[] args) throws CLIException;
}
