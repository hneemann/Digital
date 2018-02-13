package de.neemann.digital.builder.tt2;

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
 * Created by hneemann on 10.03.17.
 */
public class StartATF150xFitter implements ExpressionToFileExporter.PostProcess {
    private final JDialog parent;
    private final File fitterExe;

    private static File getFitterExe(String fitterName) {
        File fitter = Settings.getInstance().get(Keys.SETTINGS_ATF1502_FITTER);
        return new File(fitter, fitterName);
    }

    /**
     * Creates a new instance
     *
     * @param parent       the parent dialog
     * @param deviceNumber number of the device
     */
    public StartATF150xFitter(JDialog parent, int deviceNumber) {
        this(parent, getFitterExe("fit" + deviceNumber + ".exe"));
    }

    /**
     * Creates a new instance
     *
     * @param parent    the parent dialog
     * @param fitterExe fitter executable
     */
    private StartATF150xFitter(JDialog parent, File fitterExe) {
        this.parent = parent;
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
            OSExecute execute = new OSExecute(args);
            execute.setEnvVar("FITTERDIR", fitterExe.getParentFile().getPath());
            execute.setWorkingDir(file.getParentFile());

            String message = execute.start();

            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, Lang.get("msg_fitterResult"), JOptionPane.INFORMATION_MESSAGE));

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
