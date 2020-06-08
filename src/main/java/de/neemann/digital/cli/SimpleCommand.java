/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.lang.Lang;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * E simple executable command
 */
public abstract class SimpleCommand implements CLICommand {
    private final String name;
    private final ArrayList<Argument<?>> arguments;

    /**
     * Creates a new command
     *
     * @param name the name of the command
     */
    public SimpleCommand(String name) {
        this.name = name;
        arguments = new ArrayList<>();
    }

    /**
     * Adds an argument to the command
     *
     * @param argument the argument
     * @param <T>      the type of the arguments value
     * @param <A>      the type of the argument
     * @return the argument itself
     */
    public <T, A extends Argument<T>> A addArgument(A argument) {
        arguments.add(argument);
        return argument;
    }

    /**
     * @return the name of the argument
     */
    public String getName() {
        return name;
    }

    @Override
    public void printDescription(PrintStream out, String prefix) {
        String message = Lang.get("cli_help_" + name);
        out.print(prefix);
        out.print(name);
        for (Argument<?> a : arguments) {
            out.print(" ");
            out.print(a);
        }
        out.println(":");

        for (Argument<?> a : arguments)
            printString(out, prefix + "  ", a + ":\t" + Lang.get("cli_help_" + name + "_" + a.getName()));

        printString(out, prefix + "  ", message);
    }

    void printString(PrintStream out, String prefix, String message) {
        boolean lastWasSpace = false;
        out.print(prefix);
        int col = prefix.length();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == '\n')
                c = ' ';
            if (c != ' ' || !lastWasSpace) {
                if (c == ' ') {
                    if (col > 70) {
                        out.print('\n');
                        out.print(prefix);
                        col = prefix.length();
                    } else {
                        out.print(c);
                        col++;
                    }
                } else {
                    out.print(c);
                    col++;
                }
            }
            lastWasSpace = c == ' ';
        }
        out.println();
    }

    @Override
    public void execute(String[] args) throws CLIException {
        int nonOptional = 0;
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String n = it.next();
            if (n.startsWith("-")) {
                if (!it.hasNext())
                    throw new CLIException(Lang.get("cli_notEnoughArgumentsGiven"), 100);
                set(n.substring(1), it.next());
            } else {
                while (nonOptional < arguments.size() && arguments.get(nonOptional).isOptional()) {
                    nonOptional++;
                }
                if (nonOptional == arguments.size())
                    throw new CLIException(Lang.get("cli_toMuchArguments"), 105);

                arguments.get(nonOptional).setString(n);
                nonOptional++;
            }
        }

        for (Argument<?> a : arguments)
            if (!a.isOptional() && !a.isSet())
                throw new CLIException(Lang.get("cli_nonOptionalArgumentMissing_N", a), 105);

        execute();
    }

    /**
     * Executes the command
     *
     * @throws CLIException CLIException
     */
    protected abstract void execute() throws CLIException;

    private void set(String arg, String value) throws CLIException {
        for (Argument<?> a : arguments)
            if (arg.equals(a.getName())) {
                a.setString(value);
                return;
            }
        throw new CLIException(Lang.get("cli_noArgument_N_available", arg), 104);
    }

}
