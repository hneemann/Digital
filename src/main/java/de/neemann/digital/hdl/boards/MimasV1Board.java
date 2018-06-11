/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;
import java.io.IOException;

/**
 * Mimas Board Version 1
 */
public class MimasV1Board extends ISE {
    private static final BoardInformation BOARD_INFO = new BoardInformation("Spartan6", "xc6slx9", "tqg144");
    private static final BoardClockInfo[] BOARD_CLOCKPINS = {
                                            new BoardClockInfo("P126", 10)
                                           };
    /**
     * Initialize a new instance
     */
    public MimasV1Board() {
    }

    @Override
    void writePin(CodePrinter out, String name, String pinNumber) throws IOException {
        if (pinNumber == null || pinNumber.length() == 0)
            throw new IOException(Lang.get("err_vhdlPin_N_hasNoNumber", name));

        String line = String.format("NET \"%s\" LOC = \"%s\" | IOSTANDARD = LVCMOS33", name, pinNumber);

        switch (pinNumber) {
            case "P126":
                line += String.format(";\nTIMESPEC TS_CLK = PERIOD \"%s\" 100 MHz HIGH 50%%;\n", name);
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

    @Override
    BoardInformation getBoardInfo() {
        return BOARD_INFO;
    }

    @Override
    public HDLClockIntegrator getClockIntegrator() {
        return new ClockIntegratorSpartan6(BOARD_CLOCKPINS);
    }
}
