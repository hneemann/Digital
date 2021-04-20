/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.builder.tt2.StartATF150xFitter;
import de.neemann.digital.builder.tt2.TT2Exporter;

import javax.swing.*;
import java.util.Date;

/**
 * List of ATF150x devices and some helper functions.
 */
public enum ATFDevice {

    //CHECKSTYLE.OFF: JavadocVariable
    ATF1502PLCC44(1502, 43, "plcc44",
            1, 2, 4, 5, 6, 8, 9, 11, 12, 14, 16, 17,
            18, 19, 20, 21, 24, 25, 26, 27, 28,
            29, 31, 33, 34, 36, 37, 38, 39, 40, 41, 43, 44),
    ATF1502TQFP44(1502, 37, "t44", "tqfp44",
            2, 3, 5, 6, 8, 10, 11,
            12, 13, 14, 15, 18, 19, 20, 21, 22,
            23, 25, 27, 28, 30, 31, 33,
            34, 35, 37, 38, 39, 40, 42, 43, 44),
    ATF1504PLCC44(1504, 43, "plcc44",
            1, 2, 4, 5, 6, 8, 9, 11, 12, 14, 16, 17,
            18, 19, 20, 21, 24, 25, 26, 27, 28,
            29, 31, 33, 34, 36, 37, 38, 39, 40, 41, 43, 44),
    ATF1504TQFP44(1504, 37, "t44", "tqfp44",
            2, 3, 5, 6, 8, 10, 11,
            12, 13, 14, 15, 18, 19, 20, 21, 22,
            23, 25, 27, 28, 30, 31, 33,
            34, 35, 37, 38, 39, 40, 42, 43, 44),
    ATF1504PLCC84(1504, 83, "plcc84", new PL()
            .pi(1, 84)
            .e(14, 23, 62, 71)  //JTAG
            .e(7, 19, 32, 42, 47, 59, 72, 82)//GND
            .e(3, 43)//V_CCINT
            .e(13, 26, 38, 53, 66, 78)//V_CCIO
            .pins()),
    ATF1504TQFP100(1504, 87, "t100", "tqfp100", new PL().
            pi(1, 100)
            .e(4, 15, 62, 73)  //JTAG
            .e(11, 26, 38, 43, 59, 74, 86, 95)//GND
            .e(39, 91)//V_CCINT
            .e(3, 18, 34, 51, 66, 82)//V_CCIO
            .e(1, 2, 5, 7, 22, 24, 27, 28, 49, 50, 53, 55, 70, 72, 77, 78) //NC
            .pins()),
    ATF1508PLCC84(1508, 81, "plcc84",
            4, 5, 6, 8, 9, 10, 11, 12,      // A
            15, 16, 17, 18, 20, 21, 22, 24, // B
            25, 27, 28, 29, 30, 31, 33, 34, // C
            35, 36, 37, 39, 40, 41, 44, 45, // D
            46, 48, 49, 50, 51, 52, 54, 55, // E
            56, 57, 58, 60, 61, 63, 64, 65, // F
            67, 68, 69, 70, 73, 74, 75, 76, // G
            77, 79, 80),                    // H
    ATF1508PQFP100(1508, 89, "q100", "pqfp100", new PL().
            pi(1, 100)
            .e(6, 17, 64, 75)//JTAG
            .e(13, 28, 40, 45, 61, 76, 88, 97) // GND
            .e(41, 93)//V_CCINT
            .e(5, 20, 36, 53, 68, 84)//V_CCIO
            .pins()),
    ATF1508TQFP100(1508, 87, "t100", "tqfp100", new PL().
            pi(1, 100)
            .e(4, 15, 62, 73)//JTAG
            .e(11, 26, 38, 43, 59, 74, 86, 95) // GND
            .e(39, 91)//V_CCINT
            .e(3, 18, 34, 51, 66, 82)//V_CCIO
            .pins()),
    ATF1508PQFP160(1508, 139, "q160", "pqfp160", new PL().
            pi(1, 160)
            .e(9, 22, 99, 112) //JTAG
            .e(17, 42, 60, 66, 95, 113, 138, 148) // GND
            .e(61, 143) //V_CCINT
            .e(8, 26, 55, 79, 104, 133) //V_CCIO
            .e(1, 2, 3, 4, 5, 6, 7, 34, 35, 36,
                    37, 38, 39, 40, 44, 45, 46,
                    47, 74, 75, 76, 77, 81, 82,
                    83, 84, 85, 86, 87, 114,
                    115, 116, 117, 118, 119,
                    120, 124, 125, 126, 127,
                    154, 155, 156, 157) // NC
            .pins());
    //CHECKSTYLE.ON: JavadocVariable

    private final int deviceNumber;
    private final String icPackage;
    private final String packageName;
    private final int clockPin;
    private final int[] pins;

    ATFDevice(int deviceNumber, int clockPin, String icPackage, int... ioPins) {
        this(deviceNumber, clockPin, icPackage, icPackage, ioPins);
    }

    ATFDevice(int deviceNumber, int clockPin, String icPackage, String packageName, int... ioPins) {
        this.deviceNumber = deviceNumber;
        this.icPackage = icPackage;
        this.packageName = packageName;
        this.clockPin = clockPin;
        this.pins = ioPins;
    }

    private String getTT2DevName() {
        return "f" + deviceNumber + "isp" + icPackage;
    }

    /**
     * @return the menu name of this device
     */
    public String getMenuName() {
        return "ATF" + deviceNumber + "/" + packageName.toUpperCase();
    }

    private TT2Exporter getTT2Exporter(String projectName) {
        TT2Exporter tt2 = new TT2Exporter(projectName);
        tt2.getPinMapping()
                .setClockPin(clockPin)
                .setAvailBidirectional(pins);
        tt2.setDevice(getTT2DevName());
        return tt2;
    }

    private StartATF150xFitter getStartFitter(ATFDialog dialog) {
        return new StartATF150xFitter(dialog, deviceNumber);
    }

    /**
     * creates a cupl exporter
     *
     * @return the cupl exporter
     */
    public ATF150xCuplExporter getCuplExporter() {
        return new ATF150xCuplExporter(getTT2DevName(), clockPin, pins);
    }

    /**
     * creates a cupl exporter
     *
     * @param username the user name to put to the exporter
     * @param date     the date to set in the exporter
     * @return the cupl exporter
     */
    public ATF150xCuplExporter getCuplExporter(String username, Date date) {
        return new ATF150xCuplExporter(username, date, getTT2DevName(), clockPin, pins);
    }

    /**
     * creates a ExpressionToFileExporter
     *
     * @param dialog      the dialog
     * @param projectName the project name
     * @return the exporter
     */
    public ExpressionToFileExporter createExpressionToFileExporter(JDialog dialog, String projectName) {
        ATFDialog d = new ATFDialog(dialog);
        return new ExpressionToFileExporter(getTT2Exporter(projectName))
                .addProcessingStep(getStartFitter(d))
                .addProcessingStep(new CreateCHN("ATF" + deviceNumber + "AS", d));
    }
}
