package de.neemann.digital.gui;

import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * History of last opened files
 *
 * @author hneemann
 */
public final class FileHistory {
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("hist");
    private static final String FILE_NUM = "fileNum";
    private static final String FILE_NAME = "name";
    private static final int MAX_SIZE = 15;

    private final ArrayList<File> files;
    private final OpenInterface opener;
    private JMenu menu;

    /**
     * Creates a new instance
     *
     * @param opener the opene interface to be used to open a file
     */
    public FileHistory(OpenInterface opener) {
        this.opener = opener;
        int n = PREFS.getInt(FILE_NUM, 0);
        files = new ArrayList<File>();
        for (int i = 0; i < n; i++) {
            String pathname = PREFS.get(FILE_NAME + i, null);
            if (pathname != null && pathname.length() > 0)
                files.add(new File(pathname));
        }
        if (n != files.size())
            saveEntries();
    }

    private void saveEntries() {
        PREFS.putInt(FILE_NUM, files.size());
        for (int i = 0; i < files.size(); i++)
            PREFS.put(FILE_NAME + i, files.get(i).getPath());
    }

    /**
     * Adds a file to the history
     *
     * @param file the file to add
     */
    public void add(File file) {
        int i = files.indexOf(file);
        if (i != 0) {
            if (i > 0)
                files.remove(i);
            files.add(0, file);

            while (files.size() > MAX_SIZE)
                files.remove(files.size() - 1);

            saveEntries();
            updateMenu();
        }
    }

    /**
     * returns the most recent file
     *
     * @return the most recent file or null if no file present
     */
    public File getMostRecent() {
        if (files.isEmpty())
            return null;
        else
            return files.get(0);
    }

    private void updateMenu() {
        if (menu != null) {
            menu.removeAll();
            for (File f : files)
                menu.add(new FileOpenEntry(f, opener).createJMenuItem());
        }
    }

    /**
     * Sets the JMenu which is to populate with the recent files
     *
     * @param menu the menu
     */
    public void setMenu(JMenu menu) {
        this.menu = menu;
        updateMenu();
    }

    private static class FileOpenEntry extends ToolTipAction {
        private final File file;
        private final OpenInterface opener;

        FileOpenEntry(File file, OpenInterface opener) {
            super(file.getName());
            this.file = file;
            this.opener = opener;
            setToolTip(file.getPath());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            opener.open(file);
        }
    }

    /**
     * Interface used to open a file
     */
    public interface OpenInterface {
        /**
         * Is called if a user wants to open a file
         *
         * @param file the file to open
         */
        void open(File file);
    }
}
