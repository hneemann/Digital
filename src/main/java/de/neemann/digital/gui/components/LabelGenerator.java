/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

/**
 * Helper to create labels
 */
public class LabelGenerator {
    private final char[] chars;
    private int count;

    /**
     * Create a new instance
     *
     * @param chars the chars to use as a label
     */
    public LabelGenerator(char... chars) {
        this.chars = chars;
    }

    /**
     * create a new label
     *
     * @return the label
     */
    public String createLabel() {
        int ind = count % chars.length;
        int suf = count / chars.length;

        count++;

        if (suf > 0)
            return "" + chars[ind] + (suf + 1);
        else
            return "" + chars[ind];
    }
}
