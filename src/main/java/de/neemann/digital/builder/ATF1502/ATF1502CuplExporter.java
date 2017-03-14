package de.neemann.digital.builder.ATF1502;

import de.neemann.digital.builder.Gal16v8.CuplExporter;
import de.neemann.digital.builder.PinMap;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * Creates a CUPL file
 *
 * @author hneemann
 */
public class ATF1502CuplExporter extends CuplExporter {

    /**
     * Creates a new project name
     */
    public ATF1502CuplExporter() {
        this(System.getProperty("user.name"), new Date(), "f1502ispplcc44");
    }

    /**
     * Creates a new project name
     *
     * @param username user name
     * @param date     date
     */
    public ATF1502CuplExporter(String username, Date date, String type) {
        super(username, date, type, new PinMap()
                .setAvailBidirectional(4, 5, 6, 8, 9, 11, 12, 14, 16, 17,
                        18, 19, 20, 21, 24, 25, 26, 27, 28,
                        29, 31, 33, 34, 36, 37, 38, 39, 40));
        setClockPin(43);
        setCreateNodes(true);
    }

    @Override
    protected void headerWritten(Writer out) throws IOException {
        out.write("\r\nar = 'b'0 ;\r\n");
    }

    @Override
    protected void sequentialWritten(Writer out, String name) throws IOException {
        out.write(name + ".ck = CLK ;\r\n");
        out.write(name + ".ar = ar ;\r\n");
    }
}
