/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

/**
 * A cli command
 */
public interface CLICommand {

    /**
     * Executes the command
     *
     * @param args the arguments
     * @throws CLIException CLIException
     */
    void execute(String[] args) throws CLIException;

    /**
     * Prints the description
     *
     * @param out    the print stream
     * @param prefix the prefex string which should
     *               printed at the beginning of each line
     */
    void printDescription(PrintStream out, String prefix);

    /**
     * Prints the description in xml format
     *
     * @param w the writer to write to
     * @throws IOException IOException
     */
    void printXMLDescription(Writer w) throws IOException;

}
