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
public class StartATF1502Fitter implements ExpressionToFileExporter.PostProcess {
    private final JDialog parent;
    private final File fitterExe;

    private static File getFitterExe(String fitterName) {
        File fitter = Settings.getInstance().get(Keys.SETTINGS_ATF1502_FITTER);
        return new File(fitter, fitterName);
    }

    /**
     * Creates a new instance
     *
     * @param parent the parent dialog
     */
    public StartATF1502Fitter(JDialog parent) {
        this(parent, getFitterExe("fit1502.exe"));
    }

    /**
     * Creates a new instance
     *
     * @param parent     the parent dialog
     * @param fitterName name of the needed fitter
     */
    StartATF1502Fitter(JDialog parent, String fitterName) {
        this(parent, getFitterExe(fitterName));
    }

    /**
     * Creates a new instance
     *
     * @param parent    the parent dialog
     * @param fitterExe fitter executable
     */
    private StartATF1502Fitter(JDialog parent, File fitterExe) {
        this.parent = parent;
        this.fitterExe = fitterExe;
    }

    @Override
    public File execute(File file) throws IOException {
        try {
            ArrayList<String> args = new ArrayList<>();

            if (isLinux())
                args.add("wine");
            args.add(fitterExe.toString());
            args.add(file.getName());

            OSExecute execute = new OSExecute(args);
            execute.setWorkingDir(file.getParentFile());

            String message = execute.start();

            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, Lang.get("msg_fitterResult"), JOptionPane.INFORMATION_MESSAGE));

            return SaveAsHelper.checkSuffix(file, "jed");
        } catch (IOException e) {
            throw new IOException(Lang.get("err_errorRunningFitter"), e);
        }
    }

    @Override
    public String toString() {
        return Lang.get("msg_startExternalFitter");
    }
}
