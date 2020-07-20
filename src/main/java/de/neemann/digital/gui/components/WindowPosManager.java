/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps positions of windows if it is necessary to replace a window by an other.
 * If a old window is found it is closed by calling dispose.
 */
public class WindowPosManager {
    private final Map<String, Window> windows;
    private final JFrame main;
    private boolean shutdown;

    /**
     * Creates a new instance
     *
     * @param main the main window
     */
    public WindowPosManager(JFrame main) {
        this.main = main;
        this.windows = new HashMap<>();
    }

    /**
     * @return the main window, maybe null
     */
    public JFrame getMainFrame() {
        return main;
    }

    /**
     * Registers a new window.
     * If an old window with the same id is found, its position and size is set to the new window.
     * After that the old window is disposed.
     *
     * @param id     the id of the window
     * @param window the window itself
     * @param <T>    the type of the window
     * @return the window for chained calls
     */
    public <T extends Window> T register(String id, T window) {
        if (windows.containsKey(id)) {
            Window oldWindow = windows.get(id);
            window.setLocation(oldWindow.getLocation());
            window.setSize(oldWindow.getSize());
            oldWindow.dispose();
        }
        if (shutdown)
            window.dispose();
        else
            windows.put(id, window);
        return window;
    }

    /**
     * Closes all registered windows and avoids a reopening them.
     */
    public void shutdown() {
        shutdown = true;
        closeAll();
    }

    /**
     * Closes all registered windows
     */
    public void closeAll() {
        for (Window w : windows.values())
            w.dispose();
    }

    /**
     * Returns true if the window with the given id is visible
     *
     * @param id the id of the window
     * @return true if window is visible
     */
    public boolean isVisible(String id) {
        Window w = windows.get(id);
        if (w == null) return false;
        return w.isVisible();
    }
}
