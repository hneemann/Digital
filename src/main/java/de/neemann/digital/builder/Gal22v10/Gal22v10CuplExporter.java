/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal22v10;

import de.neemann.digital.builder.Gal16v8.CuplExporter;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * Creates a CUPL file
 */
public class Gal22v10CuplExporter extends CuplExporter {

    /**
     * Creates a new project name
     */
    public Gal22v10CuplExporter() {
        this(System.getProperty("user.name"), new Date());
    }

    /**
     * Creates a new project name
     *
     * @param username user name
     * @param date     date
     */
    public Gal22v10CuplExporter(String username, Date date) {
        super(username, date, "g22v10");
        getPinMapping()
                .setAvailInputs(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13)
                .setAvailOutputs(14, 15, 16, 17, 18, 19, 20, 21, 22, 23);
    }

    @Override
    protected void headerWritten(Writer out) throws IOException {
        out.write("\r\nar = 'b'0 ;\r\n"
                + "sp = 'b'0 ;\r\n");
    }

    @Override
    protected void sequentialWritten(Writer out, String name) throws IOException {
        out.write(name + ".ar = ar ;\r\n");
        out.write(name + ".sp = sp ;\r\n");
    }
}
