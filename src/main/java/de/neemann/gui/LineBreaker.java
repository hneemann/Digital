/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

/**
 * Used to break lines.
 */
public class LineBreaker {
    private static final int DEF_COLS = 70;

    private final String label;
    private final int indent;
    private final int cols;
    private final StringBuilder outText;
    private String lineBreak = "\n";
    private boolean isFirst;
    private int pos;
    private boolean preserveLineBreaks = false;
    private boolean toHTML = false;
    private int lines;

    /**
     * Creates a new instance
     */
    public LineBreaker() {
        this(DEF_COLS);
    }

    /**
     * Creates a new instance
     *
     * @param cols number of columns to use
     */
    public LineBreaker(int cols) {
        this("", 0, cols);
    }

    /**
     * Creates a new instance
     *
     * @param label  the lable to use in the first line
     * @param indent columns to indent
     * @param cols   number of columns to use
     */
    public LineBreaker(String label, int indent, int cols) {
        this.label = label;
        this.indent = indent;
        this.cols = cols;
        outText = new StringBuilder(label);
        isFirst = true;
    }

    /**
     * Sets the string inserted as a line break.
     *
     * @param lineBreak the line break, defaults to "\n".
     * @return this for chained calls
     */
    public LineBreaker setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
        return this;
    }

    /**
     * Breaks the line
     *
     * @param text the text to handle
     * @return the text containing the line breaks
     */
    public String breakLines(String text) {
        if (text == null)
            return null;

        if (text.startsWith("<html>"))
            return text;

        for (int i = 0; i < indent - label.length(); i++)
            outText.append(" ");

        StringBuilder word = new StringBuilder();
        pos = indent;
        boolean lastLineBreak=false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n':
                    if (preserveLineBreaks || lastLineBreak) {
                        addWord(word);
                        lineBreak();
                    } else {
                        addWord(word);
                        lastLineBreak = true;
                    }
                    break;
                case '\r':
                case '\t':
                case ' ':
                    addWord(word);
                    lastLineBreak = false;
                    break;
                default:
                    word.append(c);
                    lastLineBreak = false;
            }
        }
        addWord(word);

        String ret = outText.toString();
        if (toHTML) {
            ret = "<html>" + ret.replace("<", "&lt;")
                    .replace(">", "&gt;") + "</html>";
            if (lines > 1)
                ret = ret.replace("\n", "<br>");
        }

        return ret;
    }

    private void addWord(StringBuilder word) {
        if (word.length() > 0) {
            if (lines == 0)
                lines = 1;
            if (pos + (isFirst ? word.length() : word.length() + 1) > cols) {
                lineBreak();
            } else {
                if (!isFirst) {
                    outText.append(" ");
                    pos++;
                }
            }
            outText.append(word);
            pos += word.length();
            word.setLength(0);
            isFirst = false;
        }
    }

    private void lineBreak() {
        if (!isFirst) {
            outText.append(lineBreak);
            for (int j = 0; j < indent; j++)
                outText.append(" ");
            pos = indent;
            isFirst = true;
            lines++;
        }
    }

    /**
     * Preserves the contained line breaks
     *
     * @return this for chained calls
     */
    public LineBreaker preserveContainedLineBreaks() {
        this.preserveLineBreaks = true;
        return this;
    }

    /**
     * Returns an HTML string
     *
     * @return this for chained calls
     */
    public LineBreaker toHTML() {
        this.toHTML = true;
        return this;
    }

    /**
     * @return the number of created lines
     */
    public int getLineCount() {
        return lines;
    }
}
