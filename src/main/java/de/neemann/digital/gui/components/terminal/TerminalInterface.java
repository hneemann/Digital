/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

/**
 * A terminal implementation
 */
public interface TerminalInterface {

    /**
     * Adds a char to the terminal dialog
     *
     * @param value the character
     */
    void addChar(char value);

    /**
     * @return the text shown
     */
    String getText();
}
