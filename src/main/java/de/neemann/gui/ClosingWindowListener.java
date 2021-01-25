/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame needs to be set to DO_NOTHING_ON_CLOSE to work.
 * Closing should by done by the {@link GUICloser#closeGUI()}.
 */
public class ClosingWindowListener extends WindowAdapter {
    private final static String SAVE_CHANGES_MESSAGE = Lang.get("win_saveChanges");
    private final static String CONFIRM_EXIT_MESSAGE = Lang.get("win_confirmExit");
    private final static String STATE_CHANGED_MESSAGE = Lang.get("win_stateChanged");
    private final static String NO_MESSAGE = Lang.get("btn_discard");
    private final static String YES_MESSAGE = Lang.get("btn_save");
    private final static String CANCEL_MESSAGE = Lang.get("btn_editFurther");
    private final Component parent;
    private final GUICloser guiCloser;

    /**
     * Create a new Instance
     *
     * @param parent      the parent component of the confirm dialog
     * @param confirmSave the ConfirmSave interface
     */
    public ClosingWindowListener(final JFrame parent, final ConfirmSave confirmSave) {
        this(parent, confirmSave, true);
    }

    /**
     * Create a new Instance
     *
     * @param parent      the parent component of the confirm dialog
     * @param confirmSave the ConfirmSave interface
     * @param doExit      if true the parent JFrame is disposed by this listener
     */
    private ClosingWindowListener(final JFrame parent, final ConfirmSave confirmSave, final boolean doExit) {
        this((Component) parent, new GUICloser() {
            @Override
            public void closeGUI() {
                if (doExit) {
                    parent.dispose();
                }
            }

            @Override
            public boolean isStateChanged() {
                return confirmSave.isStateChanged();
            }

            @Override
            public void saveChanges() {
                confirmSave.saveChanges();
            }
        });
    }

    /**
     * Used to check for save! No Window closing is performed!
     *
     * @param parent      the Parent frame
     * @param confirmSave the confirmSafe interface
     * @return true if to proceed
     */
    public static boolean checkForSave(JFrame parent, ConfirmSave confirmSave) {
        if (confirmSave.isStateChanged()) {
            int r = new ConfirmDialogBuilder(SAVE_CHANGES_MESSAGE)
                    .setTitle(STATE_CHANGED_MESSAGE)
                    .setNoOption(NO_MESSAGE)
                    .setYesOption(YES_MESSAGE)
                    .setCancleOption(CANCEL_MESSAGE)
                    .show(parent);

            if (r == JOptionPane.YES_OPTION || r == JOptionPane.NO_OPTION) {
                if (r == JOptionPane.YES_OPTION) {
                    confirmSave.saveChanges();
                    return !confirmSave.isStateChanged();
                } else
                    return true;
            } else
                return false;
        }
        return true;
    }

    /**
     * Create a new Instance
     *
     * @param parent    the parent component of the confirm dialog
     * @param guiCloser the guiCloser
     */
    private ClosingWindowListener(Component parent, GUICloser guiCloser) {
        this.parent = parent;
        this.guiCloser = guiCloser;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (guiCloser.isStateChanged()) {
            int r = new ConfirmDialogBuilder(SAVE_CHANGES_MESSAGE)
                    .setTitle(CONFIRM_EXIT_MESSAGE)
                    .setNoOption(NO_MESSAGE)
                    .setYesOption(YES_MESSAGE)
                    .setCancleOption(CANCEL_MESSAGE)
                    .show(parent);

            if (r == JOptionPane.YES_OPTION || r == JOptionPane.NO_OPTION) {
                if (r == JOptionPane.YES_OPTION) {
                    guiCloser.saveChanges();
                    if (!guiCloser.isStateChanged())
                        guiCloser.closeGUI();
                } else
                    guiCloser.closeGUI();
            }
        } else {
            guiCloser.closeGUI();
        }
    }

    /**
     * Interface to control the gui closing
     */
    public interface ConfirmSave {
        /**
         * @return true is state is changed and there is something to save
         */
        boolean isStateChanged();

        /**
         * save changes
         */
        void saveChanges();

    }

    /**
     * Interface to control the gui closing
     */
    public interface GUICloser extends ConfirmSave {

        /**
         * Close the GUI
         */
        void closeGUI();
    }

}
