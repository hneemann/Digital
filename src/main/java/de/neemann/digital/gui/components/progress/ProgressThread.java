package de.neemann.digital.gui.components.progress;

import javax.swing.*;

/**
 * ProgressThread delegates the method calls to its parents using
 * {@link SwingUtilities#invokeLater(Runnable)}.
 * Created by hneemann on 04.03.17.
 */
public class ProgressThread implements ProgressListener {
    private final ProgressListener parent;

    /**
     * Create a new instance
     *
     * @param parent the {@link ProgressListener} to delegate the method calls to.
     */
    public ProgressThread(ProgressListener parent) {
        this.parent = parent;
    }

    @Override
    public void setStart(int max) {
        SwingUtilities.invokeLater(() -> parent.setStart(max));
    }

    @Override
    public void inc() {
        SwingUtilities.invokeLater(parent::inc);
    }

    @Override
    public void finish() {
        SwingUtilities.invokeLater(parent::finish);
    }
}
