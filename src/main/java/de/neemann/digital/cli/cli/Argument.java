/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

import de.neemann.digital.lang.Lang;

/**
 * A command cline argument
 *
 * @param <T> the type of the argument
 */
public class Argument<T> extends ArgumentBase<T> {
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

    @Override
    public T get() {
        return value;
    }

    @Override
    public void setString(String val) throws CLIException {
        value = (T) fromString(val, value);
        isSet = true;
    }

    /**
     * Creates a value from a string
     *
     * @param val      the value as a string
     * @param defValue the default value
     * @return the value converted to the type of the default value
     * @throws CLIException CLIException
     */
    public static Object fromString(String val, Object defValue) throws CLIException {
        if (defValue instanceof String)
            return val;
        else if (defValue instanceof Boolean)
            switch (val.toLowerCase()) {
                case "yes":
                case "1":
                case "true":
                    return true;
                case "no":
                case "0":
                case "false":
                    return false;
                default:
                    throw new CLIException(Lang.get("cli_notABool_N", val), 106);
            }
        else if (defValue instanceof Integer) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw new CLIException(Lang.get("cli_notANumber_N", val), e);
            }
        } else
            throw new CLIException(Lang.get("cli_invalidType_N", defValue.getClass().getSimpleName()), 203);
    }

    @Override
    public boolean isSet() {
        return isSet;
    }

    @Override
    public String getDescription(String command) {
        return Lang.get("cli_help_" + command + "_" + name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }
}
