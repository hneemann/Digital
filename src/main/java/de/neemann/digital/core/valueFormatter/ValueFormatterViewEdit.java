/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * Base class of all formatters where the string to edit and the string to show are the same
 */
public abstract class ValueFormatterViewEdit implements ValueFormatter {
    @Override
    public String formatToView(Value inValue) {
        if (inValue.isHighZ())
            return inValue.toString();
        else
            return format(inValue);
    }

    @Override
    public String formatToEdit(Value inValue) {
        if (inValue.isHighZ())
            return "Z";
        else
            return format(inValue);
    }

    /**
     * Formats the value
     *
     * @param value the value
     * @return the string representation of the value
     */
    protected abstract String format(Value value);
}
