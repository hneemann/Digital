package de.neemann.digital.builder.ATF1502;

/**
 * Creates a TT2 file suitable for the ATF1508
 *
 * @author hneemann
 */
public class ATF1508TT2Exporter extends ATF1502TT2Exporter {

    /**
     * Creates a new project name
     *
     * @param projectName project name
     */
    public ATF1508TT2Exporter(String projectName) {
        super(projectName);
        setDevice("f1508ispplcc84");
    }


}
