package de.neemann.digital.builder.ATF1502;

import de.neemann.digital.builder.tt2.TT2Exporter;

/**
 * Creates a TT2 file suitable for the ATF1508
 *
 * @author hneemann
 */
public class ATF1508TT2Exporter extends TT2Exporter {

    /**
     * Creates a new project name
     *
     * @param projectName project name
     */
    public ATF1508TT2Exporter(String projectName) {
        super(projectName);
        setDevice("f1508ispplcc84");
        getPinMapping()
                .setAvailBidirectional(
                        4, 5, 6, 8, 9, 10, 11, 12,      // A
                        15, 16, 17, 18, 20, 21, 22, 24, // B
                        25, 27, 28, 29, 30, 31, 33, 34, // C
                        35, 36, 37, 39, 40, 41, 44, 45, // D
                        46, 48, 49, 50, 51, 52, 54, 55, // E
                        56, 57, 58, 60, 61, 63, 64, 65, // F
                        67, 68, 69, 70, 73, 74, 75, 76, // G
                        77, 79, 80                      // H
                )
                .setClockPin(81);
    }

}
