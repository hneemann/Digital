/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.text;

/**
 * Used to format indexes
 */
public class Index implements Text {
    private Text var;
    private Text superScript;
    private Text subScript;

    /**
     * Creates the instance
     *
     * @param var the variable name
     */
    public Index(Text var) {
        this.var = var;
    }

    /**
     * @return the variable
     */
    public Text getVar() {
        return var;
    }

    /**
     * @return the super script
     */
    public Text getSuperScript() {
        return superScript;
    }

    /**
     * @return the sub script
     */
    public Text getSubScript() {
        return subScript;
    }

    @Override
    public Text simplify() {
        if (superScript == null && subScript == null)
            return var;
        else
            return this;
    }

    @Override
    public String toString() {
        String s = var.toString();
        if (superScript != null)
            s += "^{" + superScript + "}";
        if (subScript != null)
            s += "_{" + subScript + '}';
        return s;
    }

    /**
     * Adds a subscript
     *
     * @param b the subscript
     */
    public void addSub(Text b) {
        subScript = addTo(subScript, b);
    }

    /**
     * Adds a superscript
     *
     * @param b the superscript
     */
    public void addSuper(Text b) {
        superScript = addTo(superScript, b);
    }

    private Text addTo(Text text, Text b) {
        if (text == null)
            return b;
        if (text instanceof Sentence) {
            ((Sentence) text).add(b);
            return text;
        } else {
            return new Sentence().add(text).add(b);
        }
    }
}
