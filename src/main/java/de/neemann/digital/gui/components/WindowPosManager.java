package de.neemann.digital.gui.components;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps positions of windows if it is necessary to replace a window by an other.
 * If a old window is found it is closed by calling dispose.
 *
 * @author hneemann
 */
public class WindowPosManager {
    private final Map<String, Window> windows;

    /**
     * Creates a new instance
     */
    public WindowPosManager() {
        this.windows = new HashMap<>();
    }

    /**
     * Registers a new window.
     * If an old window with the same id is found, its position and size is set to the new window.
     * After that the old window is disposed.
     *
     * @param id     the id of the window
     * @param window the window itself
     * @return the window for chained calls
     */
    public <T extends Window> T register(String id, T window) {
        if (windows.containsKey(id)) {
            Window oldWindow = windows.get(id);
            window.setLocation(oldWindow.getLocation());
            window.setSize(oldWindow.getSize());
            oldWindow.dispose();
        }
        windows.put(id, window);
        return window;
    }

    /**
     * Closes all registered windows
     */
    public void closeAll() {
        for (Window w : windows.values())
            w.dispose();
    }
}
