package de.neemann.digital.builder.tt2;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Settings;

import javax.swing.*;
import java.io.File;

/**
 * StartATF1504Fitter start the fitter for ATF1504
 * Created by hneemann on 12.03.17.
 */
public class StartATF1504Fitter extends StartATF1502Fitter {
    /**
     * Creates a new intance
     *
     * @param parent the parent dialog
     */
    public StartATF1504Fitter(JDialog parent) {
        super(parent, getATF1504(Settings.getInstance().get(Keys.SETTINGS_ATF1502_FITTER)));
    }

    private static File getATF1504(File file) {
        return new File(file.getParentFile(), "fit1504.exe");
    }
}
