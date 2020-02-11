/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * History of last opened files
 */
public final class FileHistory {
    private static boolean isGuiTest = false;
    private static final String FILE_NUM = "fileNum";
    private static final String FILE_NAME = "name";
    private static final int MAX_SIZE = 15;

    private final ArrayList<File> files;
    private final OpenInterface opener;
    private final Preferences prefs;
    private JMenu menu;
    private JMenu menuNewWindow;

    /**
     * Sets the FileHistory to gui test mode which disables the storage
     */
    public static void setGuiTest() {
        isGuiTest = true;
    }

    /**
     * Creates a new instance
     *
     * @param opener the opene interface to be used to open a file
     */
    public FileHistory(OpenInterface opener) {
        this(opener, Preferences.userRoot().node("dig").node("hist"));
    }

    /**
     * Creates a new instance
     *
     * @param opener the opene interface to be used to open a file
     * @param prefs  the preferences node to store the history
     */
    public FileHistory(OpenInterface opener, Preferences prefs) {
        this.opener = opener;
        this.prefs = prefs;
        int n = prefs.getInt(FILE_NUM, 0);
        files = new ArrayList<File>();
        for (int i = 0; i < n; i++) {
            String pathname = prefs.get(FILE_NAME + i, null);
            if (pathname != null && pathname.length() > 0) {
                final File file = new File(pathname);
                if (file.exists())
                    files.add(file);
            }
        }
        if (n != files.size())
            saveEntries();
    }

    private void saveEntries() {
        if (!isGuiTest) {
            prefs.putInt(FILE_NUM, files.size());
            for (int i = 0; i < files.size(); i++)
                prefs.put(FILE_NAME + i, files.get(i).getPath());
        }
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

    /**
     * @return a list of files
     */
    public Iterable<File> getFiles() {
        return files;
    }

    private void updateMenu() {
        if (menu != null) {
            menu.removeAll();
            if (menuNewWindow != null)
                menuNewWindow.removeAll();
            for (File f : files) {
                menu.add(new FileOpenEntry(f, opener, false).createJMenuItem());
                if (menuNewWindow != null)
                    menuNewWindow.add(new FileOpenEntry(f, opener, true).createJMenuItem());
            }
        }
    }

    /**
     * Sets the JMenu which is to populate with the recent files
     *
     * @param menu          the menu
     * @param menuNewWindow menu to open in a new window
     */
    public void setMenu(JMenu menu, JMenu menuNewWindow) {
        this.menu = menu;
        this.menuNewWindow = menuNewWindow;
        updateMenu();
    }

    private static class FileOpenEntry extends ToolTipAction {
        private final File file;
        private final OpenInterface opener;
        private boolean newWindow;

        FileOpenEntry(File file, OpenInterface opener, boolean newWindow) {
            super(file.getName());
            this.file = file;
            this.opener = opener;
            this.newWindow = newWindow;
            setToolTip(file.getPath());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            opener.open(file, newWindow);
        }
    }

    /**
     * Interface used to open a file
     */
    public interface OpenInterface {
        /**
         * Is called if a user wants to open a file
         *
         * @param file      the file to open
         * @param newWindow if true, a new window is opened
         */
        void open(File file, boolean newWindow);
    }
}
