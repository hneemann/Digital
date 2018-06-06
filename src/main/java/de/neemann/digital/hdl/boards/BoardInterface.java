/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.HDLModel;

import java.io.File;
import java.io.IOException;

/**
 * Interface to write the additional files
 */
public interface BoardInterface {
    /**
     * Writes additional files
     *
     * @param path  the full path of the created vhdl file
     * @param model the model
     * @throws IOException IOException
     */
    void writeFiles(File path, HDLModel model) throws IOException;

    /**
     * @return returns the board specific clock integrator
     */
    HDLClockIntegrator getClockIntegrator();

}
