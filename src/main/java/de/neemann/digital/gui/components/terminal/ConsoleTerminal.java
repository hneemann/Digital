/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

/**
 * Simple termianl to print to console
 */
public class ConsoleTerminal implements TerminalInterface {
    /**
     * Max amount of characters stored
     */
    public static final int MAX_TERMINAL_STORED = 2048;

    private final StringBuilder text;

    /**
     * Creates a new instance
     */
    public ConsoleTerminal() {
        text = new StringBuilder();
    }

    @Override
    public void addChar(char value) {
        if (text.length() < MAX_TERMINAL_STORED)
            text.append(value);
        System.out.print(value);
    }

    @Override
    public String getText() {
        return text.toString();
    }
}
