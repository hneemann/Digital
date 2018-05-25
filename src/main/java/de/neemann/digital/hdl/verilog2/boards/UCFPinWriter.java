/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2.boards;

import de.neemann.digital.hdl.printer.CodePrinter;
import java.io.IOException;

/**
 *
 * @author ideras
 */
public interface UCFPinWriter {

    /**
     * Write the pin information to a Xilinx UCF (User Constraints File)
     *
     * @param out          the code printer
     * @param name         the signal name
     * @param pinNumber    the pin name
     * @throws IOException IOException
     */
    void writePin(CodePrinter out, String name, String pinNumber) throws IOException;
}
