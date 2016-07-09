package de.neemann.digital.gui.sync;

import de.neemann.digital.core.NodeException;

/**
 * @author hneemann
 */
public final class NoSync implements Sync {
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
