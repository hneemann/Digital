/*
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;

/**
 * A simple hello command.
 */
public class HelloCommand extends BasicCommand {
    private final Argument<String> name;

    /**
     * Creates the hello command.
     */
    public HelloCommand() {
        super("hello");
        name = addArgument(new Argument<>("name", "", false));
    }

    @Override
    protected void execute() {
        System.out.println("Hello " + name.get());
    }
}
