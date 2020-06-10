/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

import java.io.PrintStream;

/**
 * he command line exception
 */
public class CLIException extends Exception {
    private final int exitCode;
    private boolean showHelp = true;

    /**
     * Creates a new instance
     *
     * @param message  the message
     * @param exitCode the exit code
     */
    public CLIException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param cause   the cause
     */
    public CLIException(String message, Throwable cause) {
        super(message, cause);
        exitCode = 200;
    }

    /**
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Prints a error message to the stream
     *
     * @param out the print stream
     */
    public void printMessage(PrintStream out) {
        out.println(getMessage());
        Throwable c = getCause();
        if (c != null) {
            if (c instanceof CLIException)
                ((CLIException) c).printMessage(out);
            else
                out.println(c.getMessage());
        }
    }

    /**
     * Hides the help text
     *
     * @return this for chained calls
     */
    public CLIException hideHelp() {
        showHelp = false;
        return this;
    }

    /**
     * @return true if help has to be shown
     */
    public boolean showHelp() {
        return showHelp;
    }
}
