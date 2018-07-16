/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

/**
 * Used to delete a char in a string and keep track of the cursor position
 */
public class CharDeleter {
    private String text;
    private int pos;

    /**
     * Creaztes a new instance
     *
     * @param text the text
     * @param pos  the cursor position
     */
    public CharDeleter(String text, int pos) {
        this.text = text;
        this.pos = pos;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the cursor position
     */
    public int getPos() {
        return pos;
    }

    /**
     * deletes the last char
     *
     * @return this for chained calls
     */
    public CharDeleter delete() {
        final int len = text.length();
        if (len > 0) {
            char last = text.charAt(len - 1);
            text = text.substring(0, len - 1);
            pos--;

            if (last == '\n') {
                pos = 0;
                int p = len - 2;
                while (p >= 0 && text.charAt(p) != '\n') {
                    pos++;
                    p--;
                }
            }
        }
        return this;
    }
}
