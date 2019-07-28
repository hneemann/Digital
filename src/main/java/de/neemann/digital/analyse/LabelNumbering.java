/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

/**
 * Helper to create unique names
 */
public class LabelNumbering {

    private final String name;
    private int pos;
    private boolean useIndex = false;

    /**
     * Creates a new instance
     *
     * @param name the original name
     */
    public LabelNumbering(String name) {
        this.name = name;
        pos = name.length();
        if (name.endsWith("^n")) {
            pos = name.length() - 2;
            useIndex = !name.contains("_");
        } else if (name.endsWith("n"))
            pos = name.length() - 1;
    }

    /**
     * Creates the unique name
     *
     * @param exists checks if a name already exists
     * @return the unique label
     */
    public String create(Exists exists) {
        if (exists.exits(name)) {
            int n = 1;
            String l;
            do {
                l = addNumber(n);
                n++;
            } while (exists.exits(l));
            return l;
        }
        return name;

    }

    private String addNumber(int n) {
        if (useIndex)
            return name.substring(0, pos) + "_" + n + name.substring(pos);
        else
            return name.substring(0, pos) + n + name.substring(pos);
    }

    /**
     * Used to check if a label exists
     */
    interface Exists {
        /**
         * Returns true if a label exists
         *
         * @param name the labels name
         * @return true if label exists
         */
        boolean exits(String name);
    }

}
