package de.neemann.digital.builder.tt2;

import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Starts a fitter to create a JEDEC file.
 * Created by hneemann on 10.03.17.
 */
public class StartFitter implements ExpressionToFileExporter.PostProcess {
    private final JDialog parent;
    private final File fitterExe;

    /**
     * Creates a new instance
     *
     * @param parent the parent dialog
     */
    public StartFitter(JDialog parent) {
        this.parent = parent;
        this.fitterExe = Settings.getInstance().get(Keys.SETTINGS_ATF1502_FITTER);
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

            return Main.checkSuffix(file, "jed");
        } catch (IOException e) {
            throw new IOException(Lang.get("err_errorRunningFitter"), e);
        }
    }

    private boolean isLinux() {
        String name = System.getProperty("os.name").toLowerCase();
        return name.contains("linux");
    }

    @Override
    public String toString() {
        return Lang.get("msg_startExternalFitter");
    }
}
