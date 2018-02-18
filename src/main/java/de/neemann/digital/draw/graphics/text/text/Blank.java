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
    public Text enforceMath() {
        return this;
    }

    @Override
    public String toString() {
        return " ";
    }
}
