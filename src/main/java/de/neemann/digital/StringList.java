/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital;

/**
 * Helper to create a list of strings.
 * Takes care of adding a separator at the right places in between the items.
 */
public class StringList {

    private final StringBuilder builder;
    private boolean first;
    private String sep;

    /**
     * Create a new instance
     */
    public StringList() {
        this(new StringBuilder());
    }

    /**
     * Creates a new instance
     *
     * @param builder the StringBuilder to use
     */
    public StringList(StringBuilder builder) {
        this.builder = builder;
        this.first = true;
        this.sep = " ";
    }

    /**
     * Sets the separator.
     * The default value is a blank.
     *
     * @param sep the separator
     * @return this for chained calls
     */
    public StringList separator(String sep) {
        this.sep = sep;
        return this;
    }

    /**
     * Adds a item to the list.
     * Adds a separator if needed.
     *
     * @param item the item to add.
     * @return this for chained calls
     */
    public StringList add(String item) {
        if (first)
            first = false;
        else
            builder.append(sep);
        builder.append(item);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
