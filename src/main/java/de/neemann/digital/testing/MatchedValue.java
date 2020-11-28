/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.data.Value;
import de.neemann.digital.lang.Lang;

/**
 * A matched value.
 * The value itself represents the result and the expected value is contained as a member variable.
 */
public class MatchedValue extends Value {
    private final Value expected;
    private final long mask;

    /**
     * Creates a new instance
     *
     * @param expected the expected value
     * @param found    the found value
     */
    MatchedValue(Value expected, ObservableValue found) {
        super(found);
        this.expected = expected;
        mask = (1L << found.getBits()) - 1;
    }

    /**
     * @return true if test is passed
     */
    boolean isPassed() {
        return isEqualTo(expected, mask);
    }

    @Override
    public String toString() {
        if (isPassed())
            return super.toString();
        else
            return Lang.get("msg_testExp_N0_found_N1", expected, super.toString());
    }

    @Override
    public State getState() {
        if (isPassed())
            return State.PASS;
        else
            return State.FAIL;
    }
}
