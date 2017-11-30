package de.neemann.digital.builder.ATF1502;

import java.util.Date;

/**
 * Creates a CUPL file
 *
 * @author hneemann
 */
public class ATF1508CuplExporter extends ATF1502CuplExporter {

    /**
     * Creates a new project name
     */
    public ATF1508CuplExporter() {
        super(System.getProperty("user.name"), new Date(), "f1508ispplcc84");
    }

}
