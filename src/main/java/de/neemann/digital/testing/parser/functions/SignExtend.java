/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser.functions;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.Expression;
import de.neemann.digital.testing.parser.ParserException;

import java.util.ArrayList;

/**
 * sign extension
 * Usage: signExt([bits],[value])
 */
public class SignExtend extends Function {

    /**
     * Creates function
     */
    public SignExtend() {
        super(2);
    }

    @Override
    public long calcValue(Context c, ArrayList<Expression> args) throws ParserException {
        int bits = (int) args.get(0).value(c);
        if (bits < 0 || bits > 63)
            throw new ParserException(Lang.get("err_invalidValue_N0_inFunction_N1", bits, "signExt"));
        long value = args.get(1).value(c);
        long mask = (1L << bits) - 1;
        long signBit = (1L << (bits - 1));
        if ((value & signBit) != 0) {
            return value & mask | (~mask);
        } else {
            return value & mask;
        }
    }
}
