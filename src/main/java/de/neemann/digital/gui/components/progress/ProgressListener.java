package de.neemann.digital.gui.components.progress;

/**
 * A Simple progress listener
 * Created by hneemann on 04.03.17.
 */
public interface ProgressListener {
    /**
     * Called at start
     *
     * @param max the number of steps
     */
    void setStart(int max);

    /**
     * A single step.
     * Is called max times.
     */
    void inc();

    /**
     * process has finished
     */
    void finish();
}
