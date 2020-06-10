/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli.cli;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;

/**
 * A cli argument based on a Key instance.
 *
 * @param <T> the type of the argument.
 */
public class ArgumentKey<T> extends ArgumentBase<T> {

    private final Key<T> key;
    private final ElementAttributes attr;
    private final String name;

    /**
     * Creates a new instance
     *
     * @param key  the key
     * @param attr the attribute to store the values
     */
    public ArgumentKey(Key<T> key, ElementAttributes attr) {
        this(key, attr, 0);
    }

    /**
     * Creates a new instance
     *
     * @param key          the key
     * @param attr         the attribute to store the values
     * @param stripFromKey number of characters to strip from the key name
     */
    public ArgumentKey(Key<T> key, ElementAttributes attr, int stripFromKey) {
        this.key = key;
        this.attr = attr;
        if (stripFromKey > 0)
            name = this.key.getKey().substring(stripFromKey);
        else
            name = this.key.getKey();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public void setString(String val) throws CLIException {
        attr.set(key, (T) Argument.fromString(val, key.getDefault()));
    }

    @Override
    public boolean isSet() {
        return attr.contains(key);
    }

    @Override
    public String getDescription(String command) {
        return key.getDescription();
    }

    @Override
    public T get() {
        return attr.get(key);
    }
}
