package de.neemann.digital.gui.components.framepos;

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
     * Registers a new window
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
}
