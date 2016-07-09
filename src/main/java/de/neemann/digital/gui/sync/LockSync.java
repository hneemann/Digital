package de.neemann.digital.gui.sync;

import de.neemann.digital.core.NodeException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hneemann
 */
public class LockSync implements Sync {
    private final ReentrantLock lock;

    public LockSync() {
        lock = new ReentrantLock();
    }

    @Override
    public void access(Runnable run){
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
