/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

/**
 * The base class of all arguments
 *
 * @param <T> the type of the argument
 */
public abstract class ArgumentBase<T> {

    /**
     * @return the name of this argument
     */
    public abstract String getName();

    /**
     * @return true if this argument is optional
     */
    public abstract boolean isOptional();

    /**
     * Sets a string value
     *
     * @param val the value to set
     * @throws CLIException CLIException
     */
    public abstract void setString(String val) throws CLIException;

    /**
     * @return if this argument was set
     */
    public abstract boolean isSet();

    /**
     * Returns the description of the argument
     *
     * @param command the name of the command this argument belongs to.
     * @return the description
     */
    public abstract String getDescription(String command);

    /**
     * @return the value of the option
     */
    public abstract T get();

    @Override
    public String toString() {
        String s;
        if (isBool())
            s = "-" + getName();
        else
            s = "-" + getName()
                    + " ["
                    + get().getClass().getSimpleName()
                    + "]";
        return optionalBrace(s);
    }

    /**
     * @return a string representation containing the default value
     */
    public String toStringDef() {
        String s;
        if (isBool())
            s = "-" + getName()
                    + "(def: "
                    + get()
                    + ")";
        else
            s = "-" + getName()
                    + " ["
                    + get().getClass().getSimpleName()
                    + "(def: "
                    + get()
                    + ")]";

        return optionalBrace(s);
    }

    private String optionalBrace(String s) {
        if (isOptional())
            return "[" + s + "]";
        return s;
    }

    /**
     * @return true if this is a bool flag
     */
    public boolean isBool() {
        return get() instanceof Boolean;
    }

    /**
     * Toggles a bool value
     *
     * @throws CLIException CLIException
     */
    public void toggle() throws CLIException {
        if (isBool()) {
            boolean b = (Boolean) get();
            setString(Boolean.toString(!b));
        }
    }
}
