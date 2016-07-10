package de.neemann.digital.gui.sync;

import de.neemann.digital.core.NodeException;

/**
 * Simple sync interface
 *
 * @author hneemann
 */
public interface Sync {

    /**
     * Calls the given runnable
     *
     * @param run the runnable to execute
     */
    void access(Runnable run);

    /**
     * Same as access, but catches an exception
     *
     * @param run the runnable to execute
     * @throws NodeException NodeException
     */
    void accessNEx(ModelRun run) throws NodeException;

    /**
     * Like runnable but throws an exception
     */
    interface ModelRun {
        void run() throws NodeException;
    }

}
