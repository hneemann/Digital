/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2.boards;

import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;
import java.io.IOException;

/**
 *
 * @author ideras
 */
public class MimasV1PinWriter implements UCFPinWriter {

    /**
     * Creates a new instance.
     */
    public MimasV1PinWriter() {
    }

    @Override
    public void writePin(CodePrinter out, String name, String pinNumber) throws IOException {
        if (pinNumber == null || pinNumber.length() == 0)
            throw new IOException(Lang.get("err_vhdlPin_N_hasNoNumber", name));

        String line = String.format("NET \"%s\" LOC = \"%s\" | IOSTANDARD = LVCMOS33", name, pinNumber);

        switch (pinNumber) {
            case "P126":
                line += String.format(";\nTIMESPEC TS_CLK = PERIOD \"%s\" 100 MHz HIGH 50%;\n", name);
                break;
            /* Switch pins */
            case "P124":
            case "P123":
            case "P121":
            case "P120":
                line += " | DRIVE = 8 | SLEW = FAST | PULLUP";
                break;
            default:
                line += " | DRIVE = 8 | SLEW = FAST";
        }

        line += ";";

        out.print(line);
    }

}
