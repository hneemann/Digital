package de.neemann.digital.draw.graphics.text.text;

/**
 * A simple text
 */
public class Simple implements Text {

    private String text;

    /**
     * Creates a new text
     *
     * @param text the text
     */
    public Simple(String text) {
        this.text = text.trim();
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    @Override
    public Text simplify() {
        if (text.length() == 0)
            return null;
        else
            return this;
    }

    @Override
    public String toString() {
        return text;
    }
}
