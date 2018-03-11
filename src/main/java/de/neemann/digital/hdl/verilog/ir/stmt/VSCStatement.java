/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import java.io.IOException;

/**
 * Represents a statement in source code form
 *
 * @author ideras
 */
public class VSCStatement extends VStatement {
    private final String stmtSourceCode;

    /**
     * Creates a new instance
     *
     * @param stmtSourceCode the statement source code
     * @param place the statement place
     */
    public VSCStatement(String stmtSourceCode, VStatementPlace place) {
        super(place);
        this.stmtSourceCode = stmtSourceCode;
    }

    /**
     * Returns the statement source code
     *
     * @return the statement source code
     */
    public String getStatementSourceCode() {
        return stmtSourceCode;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        out.print(stmtSourceCode);
    }
}
