/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.gui.Settings;

import java.util.ArrayList;

/**
 * Used to split option strings to a option list
 */
public class Options {

    private final ArrayList<String> list;

    /**
     * Creates a new instance
     */
    public Options() {
        list = new ArrayList<>();
    }

    /**
     * Adds a string from the settings
     *
     * @param key the key to use
     * @return this for chained calls
     */
    public Options addFromSettings(Key<String> key) {
        return addString(Settings.getInstance().get(key));
    }

    /**
     * Adds a string containing many options
     *
     * @param options the string containing the options
     * @return this for chained calls
     */
    public Options addString(String options) {
        StringBuilder opt = new StringBuilder();
        boolean inQuote = false;
        int quoteCount = 0;
        for (int i = 0; i < options.length(); i++) {
            char c = options.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                quoteCount++;
            }

            if (c == '\\' && i < options.length() - 1 && !inQuote) {
                //modification of loop variable i is intended!
                //CHECKSTYLE.OFF: ModifiedControlVariable
                i++;
                //CHECKSTYLE.ON: ModifiedControlVariable
                opt.append(escapeChar(options.charAt(i)));
            } else {
                if (Character.isWhitespace(c) && !inQuote) {
                    if (opt.length() > 0) {
                        addQuote(opt.toString(), quoteCount);
                        quoteCount = 0;
                    }
                    opt.setLength(0);
                } else {
                    opt.append(c);
                }
            }
        }
        if (opt.length() > 0)
            addQuote(opt.toString(), quoteCount);
        return this;
    }

    private void addQuote(String opt, int quoteCount) {
        if (quoteCount == 2 && opt.charAt(0) == '"' && opt.charAt(opt.length() - 1) == '"') {
            list.add(opt.substring(1, opt.length() - 1));
        } else {
            list.add(opt);
        }
    }

    private char escapeChar(char c) {
        switch (c) {
            case 't':
                return '\t';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            default:
                return c;
        }
    }

    /**
     * Adds a single raw option
     *
     * @param option the options to add
     * @return this for chained calls
     */
    public Options add(String option) {
        list.add(option);
        return this;
    }

    /**
     * Adds a string from the give attributes
     *
     * @param attr the attributes
     * @param key  the key to use
     * @return this for chained calls
     */
    public Options add(ElementAttributes attr, Key<String> key) {
        return addString(attr.get(key));
    }


    /**
     * @return the options as a list
     */
    public ArrayList<String> getList() {
        return list;
    }

    /**
     * @return the options as an array
     */
    public String[] getArray() {
        return list.toArray(new String[0]);
    }
}
