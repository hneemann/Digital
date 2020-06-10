/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The command muxer
 */
public class Muxer extends NamedCommand {
    private final HashMap<String, CLICommand> commands;

    /**
     * Creates a new muxer
     *
     * @param name the name of the muxer
     */
    public Muxer(String name) {
        super(name);
        this.commands = new HashMap<>();
    }

    /**
     * Adds a command to the muxer
     *
     * @param command the command
     * @return this for chained calls
     */
    public Muxer addCommand(NamedCommand command) {
        return addCommand(command.getName(), command);
    }

    /**
     * Adds a command to the muxer
     *
     * @param name    the name of the command
     * @param command the command
     * @return this for chained calls
     */
    public Muxer addCommand(String name, CLICommand command) {
        commands.put(name, command);
        return this;
    }

    @Override
    public void printDescription(PrintStream out, String prefix) {
        out.print(prefix);
        out.print(getName());
        out.println();
        for (CLICommand c : commands.values())
            c.printDescription(out, prefix + "  ");
    }

    @Override
    public void printXMLDescription(Writer w) throws IOException {
        w.write("<indent>\n");
        w.write(getName());
        for (CLICommand c : commands.values())
            c.printXMLDescription(w);
        w.write("</indent>\n");
    }

    @Override
    public void execute(String[] args) throws CLIException {
        if (args.length == 0)
            throw new CLIException(Lang.get("cli_notEnoughArgumentsGiven"), 100);

        CLICommand command = commands.get(args[0]);
        if (command == null)
            throw new CLIException(Lang.get("cli_command_N_hasNoSubCommand_N", getName(), args[0]), 101);

        command.execute(Arrays.copyOfRange(args, 1, args.length));
    }

}
