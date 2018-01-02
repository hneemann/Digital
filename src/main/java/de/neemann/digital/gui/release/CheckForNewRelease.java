package de.neemann.digital.gui.release;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.InfoDialog;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Helper to check for a new release.
 */
public final class CheckForNewRelease {

    private static class InstanceHolder {
        private static final CheckForNewRelease INSTANCE = new CheckForNewRelease();
    }

    /**
     * @return the instance
     */
    public static CheckForNewRelease getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final long ADAY = 24L * 60 * 60 * 1000;
    private static final String PREF_LAST = "last";
    private static final String PREF_ASKED = "asked";
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("rev");
    private String actual;

    private CheckForNewRelease() {
    }

    /**
     * Returns true if there is a new release.
     * Connects the server only once a day and returns true only once for every new release.
     *
     * @param runnable started if there is a new release!
     */
    private void startIfNewRelease(Runnable runnable) {
        long lastAsked = PREFS.getLong(PREF_LAST, -1);
        long time = System.currentTimeMillis();
        if (time - lastAsked < ADAY) return;
        PREFS.putLong(PREF_LAST, time);

        Thread thread = new Thread(() -> {
            String rev = InfoDialog.getInstance().getRevision();
            if (rev.equals(InfoDialog.UNKNOWN)) return;

            try {
                ReleaseInfo info = new ReleaseInfo();
                actual = info.getVersion();
                if (actual != null) {

                    String asked = PREFS.get(PREF_ASKED, "none");

                    if (asked.equals(actual))
                        return;

                    PREFS.put(PREF_ASKED, actual);
                    SwingUtilities.invokeLater(runnable);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Shows a new release pop up
     *
     * @param parent the parent window
     */
    public void showReleaseDialog(Component parent) {
        startIfNewRelease(() -> {
            String msg = Lang.get("msg_newRelease_N", actual);
            InfoDialog.showInfo(parent, msg, "");
        });
    }

}
