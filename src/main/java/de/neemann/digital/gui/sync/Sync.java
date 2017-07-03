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
     * @param <A> the type oth the runnable
     * @return the given runnable. Used for chained calls
     */
    <A extends Runnable> A access(A run);

    /**
     * Same as access, but catches an exception
     *
     * @param run the runnable to execute
     * @param <A> the type oth the runnable
     * @return the given runnable. Used for chained calls
     * @throws NodeException NodeException
     */
    <A extends Sync.ModelRun> A accessNEx(A run) throws NodeException;

    /**
     * Like runnable but throws an exception
     */
    interface ModelRun {
        void run() throws NodeException;
    }

}
