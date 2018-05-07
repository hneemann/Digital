/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import de.neemann.digital.builder.ATF150x.ATFDialog;
import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static de.neemann.gui.Screen.isLinux;

/**
 * Starts a fitter to create a JEDEC file.
 */
public class StartATF150xFitter implements ExpressionToFileExporter.PostProcess {
    private final File fitterExe;
    private final ATFDialog atfDialog;

    private static File getFitterExe(String fitterName) {
        File fitter = Settings.getInstance().get(Keys.SETTINGS_ATF1502_FITTER);
        return new File(fitter, fitterName);
    }

    /**
     * Creates a new instance
     *
     * @param atfDialog    the dialog to show the result
     * @param deviceNumber number of the device
     */
    public StartATF150xFitter(ATFDialog atfDialog, int deviceNumber) {
        this(atfDialog, getFitterExe("fit" + deviceNumber + ".exe"));
    }

    /**
     * Creates a new instance
     *
     * @param atfDialog the dialog to show the result
     * @param fitterExe fitter executable
     */
    private StartATF150xFitter(ATFDialog atfDialog, File fitterExe) {
        this.atfDialog = atfDialog;
        this.fitterExe = fitterExe;
    }

    @Override
    public File execute(File file) throws IOException {
        final String tt2Name = file.getName();
        if (tt2Name.indexOf(' ') >= 0)
            throw new IOException(Lang.get("err_whiteSpaceNotAllowedInTT2Name"));

        ArrayList<String> args = new ArrayList<>();
        if (isLinux())
            args.add("wine");
        args.add(fitterExe.getPath());
        args.add(tt2Name);

        try {
            String message = new OSExecute(args)
                    .setEnvVar("FITTERDIR", fitterExe.getParentFile().getPath())
                    .setWorkingDir(file.getParentFile())
                    .startAndWait();

            SwingUtilities.invokeLater(() -> atfDialog.setFitterResult(message));

            return SaveAsHelper.checkSuffix(file, "jed");
        } catch (IOException e) {
            throw new IOException(Lang.get("err_errorRunningFitter"), e);
        }
    }

    @Override
    public String getName() {
        return Lang.get("msg_startExternalFitter");
    }
}
