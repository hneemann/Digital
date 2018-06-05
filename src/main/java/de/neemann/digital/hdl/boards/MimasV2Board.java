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
 * Mimas Board Version 2
 */
public class MimasV2Board extends ISE {
    private static final BoardInformation BOARD_INFO = new BoardInformation("Spartan6", "xc6slx9", "csg324");
    private static final BoardClockInfo[] BOARD_CLOCKPINS = {
                                            new BoardClockInfo("V10", 10),
                                            new BoardClockInfo("D9", 83.33)
                                           };
    /**
     * Initialize a new instance
     */
    public MimasV2Board() {
    }

    @Override
    void writePin(CodePrinter out, String name, String pinNumber) throws IOException {
        if (pinNumber == null || pinNumber.length() == 0)
            throw new IOException(Lang.get("err_vhdlPin_N_hasNoNumber", name));

        String line = String.format("NET \"%s\" LOC = \"%s\" | IOSTANDARD = LVCMOS33", name, pinNumber);

        switch (pinNumber) {
            case "V10":
                line += " | PERIOD = 100MHz";
                break;
            case "D9":
                line += " | PERIOD = 12MHz";
                break;
            case "C17":
            case "C18":
            case "D17":
            case "D18":
            case "E18":
            case "E16":
            case "F18":
            case "F17":
            case "M18":
            case "L18":
            case "M16":
            case "L17":
            case "K17":
            case "K18":
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
