package de.neemann.digital.draw.graphics.text.text;

/**
 * A single character. Used to represent special characters
 */
public class Character implements Text {
    private final char aChar;

    /**
     * Creates a new instance
     *
     * @param aChar the character
     */
    public Character(char aChar) {
        this.aChar = aChar;
    }

    @Override
    public Text simplify() {
        return this;
    }

    @Override
    public Text enforceMath() {
        if (aChar == '≥' || aChar == '¬')
            return new Decorate(this, Decorate.Style.MATH);
        else
            return this;
    }

    @Override
    public String toString() {
        return "" + aChar;
    }

    /**
     * @return the represented character
     */
    public char getChar() {
        return aChar;
    }
}
