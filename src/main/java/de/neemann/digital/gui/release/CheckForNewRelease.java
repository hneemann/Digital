/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
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

    private static final long ADAY = 24L * 60 * 60 * 1000;
    private static final String PREF_LAST = "last";
    private static final String PREF_ASKED = "asked";
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("rev");

    private CheckForNewRelease() {
    }

    /**
     * Connects the server only once a day and calls the interface method only
     * once for every new release.
     *
     * @param anInterface started if there is a new release!
     */
    static private void startIfNewRelease(Interface anInterface) {
        long lastAsked = PREFS.getLong(PREF_LAST, -1);
        long time = System.currentTimeMillis();
        if (time - lastAsked < ADAY) return;
        PREFS.putLong(PREF_LAST, time);

        Thread thread = new Thread(() -> {
            String runningRev = InfoDialog.getInstance().getRevision();
            if (runningRev.equals(InfoDialog.UNKNOWN) || runningRev.length() > 7) return;

            try {
                ReleaseInfo info = new ReleaseInfo();
                String latestRev = info.getVersion();
                if (latestRev != null) {

                    String asked = PREFS.get(PREF_ASKED, "none");

                    if (asked.equals(latestRev))
                        return;

                    PREFS.put(PREF_ASKED, latestRev);

                    if (runningRev.equals(latestRev))
                        return;

                    SwingUtilities.invokeLater(() -> anInterface.showMessage(latestRev));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Shows a new release pop up in necessary
     *
     * @param parent the parent window
     */
    static public void showReleaseDialog(Window parent) {
        startIfNewRelease((latestRev) -> {
            String msg = Lang.get("msg_newRelease_N", latestRev);
            InfoDialog.showInfo(parent, msg, "");
        });
    }

    private interface Interface {
        void showMessage(String latest);
    }

}
