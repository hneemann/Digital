/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.fsm.gui.FSMFrame;
import de.neemann.digital.gui.Main;

import java.io.File;

/**
 * The model state info
 */
public class FSMStateInfo implements Main.CreatedNotification {
    private final String signalName;
    private final FSMFrame fsmFrame;

    /**
     * Creates a new instance
     *
     * @param file     the file of the fsm, maybe null
     * @param fsmFrame the creator, maybe null
     */
    public FSMStateInfo(File file, FSMFrame fsmFrame) {
        this.fsmFrame = fsmFrame;
        if (file != null)
            this.signalName = file.getName();
        else
            this.signalName = "state";
    }

    /**
     * @return the signal name used for state transfer
     */
    public String getSignalName() {
        return signalName;
    }

    @Override
    public void isCreated(Main main) {
        if (fsmFrame != null)
            fsmFrame.registerTo(main);
    }
}
