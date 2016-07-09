package de.neemann.digital.gui.sync;

import de.neemann.digital.core.NodeException;

/**
 * Implementation which is used in runtim clock does not run.
 * Does no synchronisation at all.
 *
 * @author hneemann
 */
public final class NoSync implements Sync {
    /**
     * The single instance
     */
    public static final Sync INST = new NoSync();

    private NoSync() {
    }

    @Override
    public void access(Runnable run) {
        run.run();
    }

    @Override
    public void accessNEx(Sync.ModelRun run) throws NodeException {
        run.run();
    }
}
