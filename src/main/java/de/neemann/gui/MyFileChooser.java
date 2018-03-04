/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * File chooser that fixes the window size issues.
 */
public class MyFileChooser extends JFileChooser {

    /**
     * Create a new instance
     */
    public MyFileChooser() {
        super();
        matchScreenSize();
    }

    /**
     * Create a new instance
     *
     * @param folder the folder to use
     */
    public MyFileChooser(File folder) {
        super(folder);
        matchScreenSize();
    }

    private void matchScreenSize() {
        if (Screen.getInstance().getScaling() != 1)
            setPreferredSize(Screen.getInstance().scale(new Dimension(505, 326)));
    }

}
