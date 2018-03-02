/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.text;

/**
 * The Blank character
 */
public final class Blank implements Text {
    /**
     * the Blank instance
     */
    public static final Blank BLANK = new Blank();

    private Blank() {
    }

    @Override
    public Text simplify() {
        return this;
    }

    @Override
    public String toString() {
        return " ";
    }
}
