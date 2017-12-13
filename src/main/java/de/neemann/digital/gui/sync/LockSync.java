package de.neemann.digital.gui.sync;

import de.neemann.digital.core.NodeException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Calls the runnables under a reentrant lock.
 *
 * @author hneemann
 */
public class LockSync implements Sync {
    private final ReentrantLock lock;

    /**
     * Creates a new instance
     */
    public LockSync() {
        lock = new ReentrantLock(true);
    }

    @Override
    public <A extends Runnable> A access(A run) {
        lock.lock();
        try {
            run.run();
            return run;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <A extends Sync.ModelRun> A  accessNEx(A run) throws NodeException {
        lock.lock();
        try {
            run.run();
            return run;
        } finally {
            lock.unlock();
        }
    }
}
