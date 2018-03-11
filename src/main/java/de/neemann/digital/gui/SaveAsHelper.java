/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Helper to handle the overwrite conformation
 * <p>
 */
public final class SaveAsHelper {
    private final Component parent;
    private final JFileChooser fc;
    private final String suffix;
    private boolean repeat;

    /**
     * Creates a new instance
     *
     * @param parent the parent
     * @param fc     the file chooser
     */
    public SaveAsHelper(Component parent, JFileChooser fc) {
        this(parent, fc, null);
    }


    /**
     * Creates a new instance
     *
     * @param parent the parent
     * @param fc     the file chooser
     * @param suffix the suffix to enforce
     */
    public SaveAsHelper(Component parent, JFileChooser fc, String suffix) {
        this.parent = parent;
        this.fc = fc;
        this.suffix = suffix;
    }

    /**
     * Uses the JFileChooser to select a file and checks, if the file exists.
     * Uses the gicen interface to save the file.
     *
     * @param saveAs used to save the file
     */
    public void checkOverwrite(SaveAs saveAs) {
        do {
            repeat = false;
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {

                final File selectedFile = checkSuffix(fc.getSelectedFile(), suffix);

                if (selectedFile.exists()) {
                    Object[] options = {Lang.get("btn_overwrite"), Lang.get("btn_newName")};
                    int res = JOptionPane.showOptionDialog(parent,
                            Lang.get("msg_fileExists", selectedFile.getName()),
                            Lang.get("msg_warning"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    if (res == 1) {
                        repeat = true;
                        continue;
                    }
                }

                try {
                    saveAs.saveAs(selectedFile);
                } catch (IOException e) {
                    new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e).show(parent);
                }
            }
        } while (repeat);
    }

    /**
     * if called user can select an other name
     */
    public void retryFileSelect() {
        repeat = true;
    }

    /**
     * Adds the given suffix to the file
     *
     * @param filename filename
     * @param suffix   suffix
     * @return the file name with the given suffix
     */
    public static File checkSuffix(File filename, String suffix) {
        if (suffix == null)
            return filename;

        String name = filename.getName();
        int p = name.lastIndexOf('.');
        if (p >= 0)
            name = name.substring(0, p);
        return new File(filename.getParentFile(), name + "." + suffix);
    }


    /**
     * Used to encapsulate the save action
     */
    public interface SaveAs {
        /**
         * Interface to implement the save operation
         *
         * @param file the file to write
         * @throws IOException IOException
         */
        void saveAs(File file) throws IOException;

    }

}
