/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.lang.Lang;

/**
 * A command cline argument
 *
 * @param <T> the type of the argument
 */
public class Argument<T> {
    private final String name;
    private final boolean optional;
    private T value;
    private boolean isSet;

    /**
     * Creates a new argument
     *
     * @param name     the name of the argument
     * @param def      the default value
     * @param optional true if argument is optional
     */
    public Argument(String name, T def, boolean optional) {
        this.name = name;
        this.optional = optional;
        if (def == null)
            throw new NullPointerException();
        value = def;
    }

    T get() {
        return value;
    }

    @Override
    public String toString() {
        if (optional)
            return "[[" + name + "]]";
        else
            return "[" + name + "]";
    }

    /**
     * Sets a string value
     *
     * @param val the value to set
     * @throws CLIException CLIException
     */
    public void setString(String val) throws CLIException {
        if (value instanceof String)
            value = (T) val;
        else if (value instanceof Boolean)
            switch (val.toLowerCase()) {
                case "yes":
                case "1":
                case "true":
                    value = (T) (Boolean) true;
                    break;
                case "no":
                case "0":
                case "false":
                    value = (T) (Boolean) false;
                    break;
                default:
                    throw new CLIException(Lang.get("cli_notABool_N", val), 106);
            }
        else if (value instanceof Integer) {
            try {
                value = (T) (Integer) Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw new CLIException(Lang.get("cli_notANumber_N", val), e);
            }
        } else
            throw new CLIException(Lang.get("cli_invalidType_N", value.getClass().getSimpleName()), 203);
        isSet = true;
    }

    /**
     * @return if this argument was set
     */
    public boolean isSet() {
        return isSet;
    }

    /**
     * @return the name of this argument
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if this argument is optional
     */
    public boolean isOptional() {
        return optional;
    }
}
