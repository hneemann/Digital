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
    public <A extends Runnable> A access(A run) {
        run.run();
        return run;
    }

    @Override
    public <A extends Sync.ModelRun> A accessNEx(A run) throws NodeException {
        run.run();
        return run;
    }
}
