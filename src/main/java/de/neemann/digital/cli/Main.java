/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.cli.cli.Muxer;

/**
 * Entry point for the command line interface
 */
public class Main extends Muxer {
    /**
     * Creates a new CLI main
     */
    public Main() {
        super("java -cp Digital.jar CLI");
        addCommand(new CommandLineTester.TestCommand());
        addCommand(new SVGExport());
        addCommand(new StatsExport());
    }

    /**
     * Evaluates the command line arguments
     *
     * @param args the cli arguments
     */
    public void main(String[] args) {
        try {
            execute(args);
        } catch (CLIException e) {
            e.printMessage(System.out);
            if (e.showHelp()) {
                System.out.println();
                printDescription(System.out, "");
            }
            System.exit(e.getExitCode());
        }
    }
}
