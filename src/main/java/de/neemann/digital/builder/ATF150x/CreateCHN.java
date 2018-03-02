/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.lang.Lang;

import java.io.*;

/**
 * CreateCHN creates the chn file which can be opened by ATMISP to flash the JEDEC to the ATM1502.
 * So it is not necessary to setup a ATMISP project manually.
 */
public class CreateCHN implements ExpressionToFileExporter.PostProcess {

    private final String device;

    /**
     * create a new instance
     *
     * @param device the device name used in the chn file
     */
    public CreateCHN(String device) {
        this.device = device;
    }

    @Override
    public File execute(File file) throws IOException {
        File chnFile = SaveAsHelper.checkSuffix(file, "chn");

        try (Writer chn = new OutputStreamWriter(new FileOutputStream(chnFile), "ISO-8859-1")) {
            chn.write("1 4 1 0 \r\n"
                    + "\r\n"
                    + device + "\r\n"
                    + "10\r\n"
                    + "1\r\n");
            chn.write(file.getPath());
            chn.write("\r\n");
        }

        return chnFile;
    }

    @Override
    public String getName() {
        return Lang.get("msg_create CHNFile");
    }
}
