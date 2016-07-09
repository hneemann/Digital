package de.neemann.digital.gui.sync;

import de.neemann.digital.core.NodeException;

/**
 * @author hneemann
 */
public interface Sync {

    void access(Runnable run);

    void accessNEx(ModelRun run) throws NodeException;

    interface ModelRun {
        void run() throws NodeException;
    }

}
