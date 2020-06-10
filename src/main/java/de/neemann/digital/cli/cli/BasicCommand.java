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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A simple executable command
 */
public abstract class BasicCommand extends NamedCommand {
    private final ArrayList<ArgumentBase<?>> arguments;

    /**
     * Creates a new command
     *
     * @param name the name of the command
     */
    public BasicCommand(String name) {
        super(name);
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
    public <T, A extends ArgumentBase<T>> A addArgument(A argument) {
        arguments.add(argument);
        return argument;
    }

    @Override
    public void printDescription(PrintStream out, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        for (ArgumentBase<?> a : arguments) {
            sb.append(" ");
            sb.append(a);
        }
        sb.append(":");
        out.print(prefix);
        printString(out, prefix + "      ", sb.toString());

        prefix += "  ";
        out.print(prefix + "  ");
        printString(out, prefix + "  ", Lang.get("cli_help_" + getName()));
        out.print(prefix);
        out.println(Lang.get("cli_options"));


        for (ArgumentBase<?> a : arguments) {
            out.println(prefix + "  " + a.toStringDef());
            out.print(prefix + "    ");
            printString(out, prefix + "    ", a.getDescription(getName()));
        }
    }

    @Override
    public void printXMLDescription(Writer w) throws IOException {
        w.write("<indent>\n");
        w.append(getName());
        for (ArgumentBase<?> a : arguments) {
            w.append(" ");
            w.append(a.toString());
        }
        w.append(":");
        w.write("<indent>\n");
        w.write(Lang.get("cli_help_" + getName()));
        w.write("</indent>\n");
        w.write("<indent>\n");
        w.write(Lang.get("cli_options"));
        for (ArgumentBase<?> a : arguments) {
            w.write("<indent>\n");
            w.write(a.toStringDef());
            w.write("<indent>\n");
            w.write(a.getDescription(getName()));
            w.write("</indent>\n");
            w.write("</indent>\n");
        }
        w.write("</indent>\n");
        w.write("</indent>\n");
    }

    void printString(PrintStream out, String prefix, String message) {
        boolean lastWasSpace = false;
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

    private void set(String arg, Iterator<String> args) throws CLIException {
        for (ArgumentBase<?> a : arguments)
            if (arg.equals(a.getName())) {
                if (a.isBool())
                    a.toggle();
                else {
                    if (!args.hasNext())
                        throw new CLIException(Lang.get("cli_notEnoughArgumentsGiven"), 100);
                    a.setString(args.next());
                }
                return;
            }
        throw new CLIException(Lang.get("cli_noArgument_N_available", arg), 104);
    }

    @Override
    public void execute(String[] args) throws CLIException {
        int nonOptional = 0;
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String n = it.next();
            if (n.startsWith("-")) {
                set(n.substring(1), it);
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

        for (ArgumentBase<?> a : arguments)
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

}
