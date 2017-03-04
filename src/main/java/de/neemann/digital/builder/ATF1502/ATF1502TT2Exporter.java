package de.neemann.digital.builder.ATF1502;

import de.neemann.digital.builder.tt2.TT2Exporter;

/**
 * Creates a TT2 file suitable for the ATF1502
 *
 * @author hneemann
 */
public class ATF1502TT2Exporter extends TT2Exporter {

    /**
     * Creates a new project name
     */
    public ATF1502TT2Exporter() {
        this(System.getProperty("user.name"));
    }

    /**
     * Creates a new project name
     *
     * @param projectName user name
     */
    public ATF1502TT2Exporter(String projectName) {
        super();
        setProjectName(projectName);
        getPinMapping().setAvailBidirectional(4, 5, 6, 8, 9, 11, 12, 14, 16, 17,
                18, 19, 20, 21, 24, 25, 26, 27, 28,
                29, 31, 33, 34, 36, 37, 38, 39, 40);
        setClockPin(43);
    }

}
