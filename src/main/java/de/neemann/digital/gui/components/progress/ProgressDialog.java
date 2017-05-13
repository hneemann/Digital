package de.neemann.digital.gui.components.progress;

import javax.swing.*;
import java.awt.*;

/**
 * A simple progress listener.
 * Created by hneemann on 04.03.17.
 */
public class ProgressDialog extends JDialog implements ProgressListener {
    private final JProgressBar bar;
    private int val;

    /**
     * Creates a new instance
     *
     * @param parent the parent dialog
     */
    public ProgressDialog(Dialog parent) {
        super(parent, true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        bar = new JProgressBar();
        getContentPane().add(bar);
        pack();
        setLocationRelativeTo(parent);
    }


    @Override
    public void setStart(int max) {
        bar.setMaximum(max);
        setVisible(true);
        val = 0;
    }

    @Override
    public void inc() {
        val++;
        bar.setValue(val);
        System.out.println(val);
    }

    @Override
    public void finish() {
        dispose();
    }
}
