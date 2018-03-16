/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Statement;

import java.util.ArrayList;

/**
 * Description of a first class function.
 */
public class FirstClassFunction {
    private final ArrayList<String> args;
    private final Statement statement;

    /**
     * Creates a new instance
     *
     * @param args      the names of the arguments
     * @param statement the function body
     */
    public FirstClassFunction(ArrayList<String> args, Statement statement) {
        this.args = args;
        this.statement = statement;
    }

    ArrayList<String> getArgs() {
        return args;
    }

    Statement getStatement() {
        return statement;
    }
}
