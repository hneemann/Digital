package de.neemann.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame needs to be set to {@link WindowConstants.DO_NOTHING_ON_CLOSE} to work.
 * Closing should by done by the {@link GUICloser#closeGUI()}.
 *
 * @author hneemann
 */
public class ClosingWindowListener extends WindowAdapter {
    public static String SAVE_CHANGES_MESSAGE = Lang.get("win_saveChanges");
    public static String CONFIRM_EXIT_MESSAGE = Lang.get("win_confirmExit");
    public static String STATE_CHANGED_MESSAGE = Lang.get("win_stateChanged");
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
     */
    public ClosingWindowListener(final JFrame parent, final ConfirmSave confirmSave, final boolean doExit) {
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
            int r = JOptionPane.showConfirmDialog(parent, SAVE_CHANGES_MESSAGE, STATE_CHANGED_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            if (r != JOptionPane.CANCEL_OPTION) {
                if (r == JOptionPane.YES_OPTION) {
                    confirmSave.saveChanges();
                }
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new Instance
     *
     * @param parent    the parent component of the confirm dialog
     * @param guiCloser the guiCloser
     */
    public ClosingWindowListener(Component parent, GUICloser guiCloser) {
        this.parent = parent;
        this.guiCloser = guiCloser;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (guiCloser.isStateChanged()) {
            int r = JOptionPane.showConfirmDialog(parent, SAVE_CHANGES_MESSAGE, CONFIRM_EXIT_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            if (r != JOptionPane.CANCEL_OPTION) {
                if (r == JOptionPane.YES_OPTION) {
                    guiCloser.saveChanges();
                }
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
