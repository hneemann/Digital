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
        lock = new ReentrantLock();
    }

    @Override
    public void access(Runnable run) {
        lock.lock();
        try {
            run.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void accessNEx(Sync.ModelRun run) throws NodeException {
        lock.lock();
        try {
            run.run();
        } finally {
            lock.unlock();
        }
    }
}
