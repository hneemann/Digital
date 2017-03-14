package de.neemann.digital.builder.ATF1502;

/**
 * Creates a TT2 file suitable for the ATF1502
 *
 * @author hneemann
 */
public class ATF1504TT2Exporter extends ATF1502TT2Exporter {

    /**
     * Creates a new project name
     */
    public ATF1504TT2Exporter(String projectName) {
        super(projectName);
        setDevice("f1504ispplcc44");
    }


}
