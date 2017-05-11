package de.neemann.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * File chooser that fixes the window size issues.
 * Created by hneemann on 11.05.17.
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
