/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.CommandLineTester;

import java.io.File;
import java.io.IOException;

/**
 * Used to test circuits.
 */
public class CLITester extends BasicCommand {
    private final Argument<String> circ;
    private final Argument<String> test;

    /**
     * Creates a new CLI command
     */
    public CLITester() {
        super("test");
        circ = addArgument(new Argument<>("circ", "", false));
        test = addArgument(new Argument<>("test", "", true));
    }

    @Override
    protected void execute() throws CLIException {
        try {
            CommandLineTester clt = new CommandLineTester(new File(circ.get()));
            if (test.isSet())
                clt.useTestCasesFrom(new File(test.get()));
            int errors = clt.execute();
            if (errors > 0)
                throw new CLIException(Lang.get("cli_thereAreTestFailures"), errors);
        } catch (IOException e) {
            throw new CLIException(Lang.get("cli_errorExecutingTests"), e);
        }
    }
}
