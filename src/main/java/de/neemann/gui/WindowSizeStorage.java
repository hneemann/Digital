/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

/**
 * Used to store the window size
 */
public final class WindowSizeStorage {
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("win");
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    private final Preferences prefs;
    private int defWidth = 1024;
    private int defHeight = 768;

    /**
     * Creates a new instance.
     *
     * @param key the key used to store the size, must be unique
     */
    public WindowSizeStorage(String key) {
        prefs = PREFS.node(key);
    }

    /**
     * Sets the default size. Used at the first startup
     *
     * @param width  width
     * @param height height
     * @return this for chained calls
     */
    public WindowSizeStorage setDefaultSize(int width, int height) {
        this.defWidth = width;
        this.defHeight = height;
        return this;
    }

    /**
     * Restore the last used size.
     *
     * @param component the component to use
     */
    public void restore(Component component) {
        int width = prefs.getInt(WIDTH_KEY, 0);
        int height = prefs.getInt(HEIGHT_KEY, 0);

        if (width < 100 || height < 80)
            component.setSize(Screen.getInstance().scale(new Dimension(defWidth, defHeight)));
        else
            component.setSize(new Dimension(width, height));

        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                final Dimension size = component.getSize();
                prefs.putInt(WIDTH_KEY, size.width);
                prefs.putInt(HEIGHT_KEY, size.height);
            }
        });
    }
}
