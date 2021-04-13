/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import de.neemann.digital.builder.Gal16v8.CuplExporter;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * Creates a CUPL file
 */
public class ATF150xCuplExporter extends CuplExporter {

    /**
     * Creates a new CUPL exporter
     *
     * @param devName  the device name
     * @param clockPin the clock pin
     * @param pins     the bidirectional pins
     */
    public ATF150xCuplExporter(String devName, int clockPin, int[] pins) {
        this(System.getProperty("user.name"), new Date(), devName, clockPin, pins);
    }

    /**
     * Creates a new project name
     *
     * @param username user name
     * @param date     date
     * @param devName  the type of the device
     * @param clockPin the clock pin
     * @param pins     the bidirectional pins
     */
    public ATF150xCuplExporter(String username, Date date, String devName, int clockPin, int[] pins) {
        super(username, date, devName);
        getPinMapping()
                .setAvailBidirectional(pins);
        setClockPin(clockPin);
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
